/*
 * Copyright (c) "Neo4j"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.gds.compression.common;

import org.apache.commons.lang3.mutable.MutableLong;
import org.neo4j.gds.api.AdjacencyProperties;
import org.neo4j.gds.api.properties.relationships.PropertyCursor;
import org.neo4j.gds.collections.PageUtil;
import org.neo4j.gds.collections.ha.HugeIntArray;
import org.neo4j.gds.collections.ha.HugeLongArray;
import org.neo4j.gds.compression.api.AdjacencyListBuilder;
import org.neo4j.gds.compression.api.MemoryTracker;
import org.neo4j.gds.compression.api.ModifiableSlice;
import org.neo4j.gds.compression.api.Slice;
import org.neo4j.gds.mem.MemoryUsage;
import org.neo4j.gds.memory.info.MemoryInfo;
import org.neo4j.gds.utils.GdsFeatureToggles;

import java.util.Arrays;
import java.util.Optional;

import static org.neo4j.gds.collections.PageUtil.indexInPage;
import static org.neo4j.gds.collections.PageUtil.pageIndex;

/**
 * A minimal adjacency properties builder backed by a {@code long[][]} BumpAllocator.
 * Builds instances of {@link UncompressedProperties} which implement {@link AdjacencyProperties}.
 * This is the properties-only counterpart to the full uncompressed adjacency list.
 */
// Temporary properties-only builder to avoid compression-varlong/compression-packed depending on
// compression-uncompressed. Task 4 has moved UncompressedAdjacencyListBuilder to compression-uncompressed;
// this class can be removed if varlong and packed are updated to depend on compression-uncompressed
// and use UncompressedAdjacencyListBuilder directly. Leave this decision to Task 7 cleanup.
public final class UncompressedPropertiesBuilder implements AdjacencyListBuilder<long[], AdjacencyProperties> {

    private final BumpAllocator<long[]> builder;
    private final MemoryTracker memoryTracker;

    public UncompressedPropertiesBuilder(MemoryTracker memoryTracker) {
        this.memoryTracker = memoryTracker;
        this.builder = new BumpAllocator<>(Factory.INSTANCE);
    }

    @Override
    public Allocator newAllocator() {
        return new Allocator(this.builder.newLocalAllocator(), this.memoryTracker);
    }

    @Override
    public AdjacencyListBuilder.PositionalAllocator<long[]> newPositionalAllocator() {
        return new PositionalAllocator(this.builder.newLocalPositionalAllocator(PositionalFactory.INSTANCE));
    }

    @Override
    public AdjacencyProperties build(HugeIntArray degrees, HugeLongArray offsets, boolean allowReordering) {
        long[][] intoPages = builder.intoPages();
        if (allowReordering) {
            reorder(intoPages, offsets, degrees);
        }
        var memoryInfo = memoryInfo(intoPages, degrees, offsets);
        return new UncompressedProperties(intoPages, degrees, offsets, memoryInfo);
    }

    @Override
    public void reorder(long[][] pages, HugeLongArray offsets, HugeIntArray degrees) {
        if (GdsFeatureToggles.USE_REORDERED_ADJACENCY_LIST.isEnabled() && pages.length > 0) {
            PageReordering.reorder(pages, offsets, degrees);
        }
    }

    private MemoryInfo memoryInfo(long[][] pages, HugeIntArray degrees, HugeLongArray offsets) {
        for (long[] page : pages) {
            this.memoryTracker.recordPageSize(page.length * Long.BYTES);
        }

        var memoryInfoBuilder = MemoryInfoUtil
            .builder(memoryTracker, Optional.empty())
            .pages(pages.length)
            .bytesOffHeap(0);

        var sizeOnHeap = new MutableLong();
        MemoryUsage.sizeOfObject(pages).ifPresent(sizeOnHeap::add);
        MemoryUsage.sizeOfObject(degrees).ifPresent(sizeOnHeap::add);
        MemoryUsage.sizeOfObject(offsets).ifPresent(sizeOnHeap::add);
        memoryInfoBuilder.bytesOnHeap(sizeOnHeap.longValue());

        return memoryInfoBuilder.build();
    }

    private enum Factory implements BumpAllocator.Factory<long[]> {
        INSTANCE;

        @Override
        public long[][] newEmptyPages() {
            return new long[0][];
        }

        @Override
        public long[] newPage(int length) {
            return new long[length];
        }
    }

    private enum PositionalFactory implements BumpAllocator.PositionalFactory<long[]> {
        INSTANCE;

        @Override
        public long[] copyOfPage(long[] longs, int length) {
            return Arrays.copyOf(longs, length);
        }

        @Override
        public int lengthOfPage(long[] longs) {
            return longs.length;
        }
    }

    public static final class Allocator implements AdjacencyListBuilder.Allocator<long[]> {

        private final BumpAllocator.LocalAllocator<long[]> allocator;
        private final MemoryTracker memoryTracker;

        private Allocator(BumpAllocator.LocalAllocator<long[]> allocator, MemoryTracker memoryTracker) {
            this.allocator = allocator;
            this.memoryTracker = memoryTracker;
        }

        @Override
        public long allocate(int allocationSize, Slice<long[]> into) {
            this.memoryTracker.recordHeapAllocation(allocationSize);
            return allocator.insertInto(allocationSize, (ModifiableSlice<long[]>) into);
        }

        @Override
        public void close() {
        }
    }

    public static final class PositionalAllocator implements AdjacencyListBuilder.PositionalAllocator<long[]> {

        private final BumpAllocator.LocalPositionalAllocator<long[]> allocator;

        private PositionalAllocator(BumpAllocator.LocalPositionalAllocator<long[]> allocator) {
            this.allocator = allocator;
        }

        @Override
        public void writeAt(long address, long[] properties, int length) {
            allocator.insertAt(address, properties, length);
        }

        @Override
        public void close() {
        }
    }

    /**
     * Minimal {@link AdjacencyProperties} implementation backed by {@code long[][]} pages.
     */
    static final class UncompressedProperties implements AdjacencyProperties {

        private final long[][] pages;
        private final HugeIntArray degrees;
        private final HugeLongArray offsets;
        @SuppressWarnings("unused")
        private final MemoryInfo memoryInfo;

        UncompressedProperties(
            long[][] pages,
            HugeIntArray degrees,
            HugeLongArray offsets,
            MemoryInfo memoryInfo
        ) {
            this.pages = pages;
            this.degrees = degrees;
            this.offsets = offsets;
            this.memoryInfo = memoryInfo;
        }

        @Override
        public PropertyCursor propertyCursor(long node, double fallbackValue) {
            int degree = degrees.get(node);
            if (degree == 0) {
                return PropertyCursor.empty();
            }
            var cursor = new Cursor(pages);
            cursor.init(offsets.get(node), degree);
            return cursor;
        }

        @Override
        public PropertyCursor propertyCursor(PropertyCursor reuse, long node, double fallbackValue) {
            int degree = degrees.get(node);
            if (degree == 0) {
                return PropertyCursor.empty();
            }
            if (reuse instanceof Cursor c) {
                c.init(offsets.get(node), degree);
                return c;
            }
            return propertyCursor(node, fallbackValue);
        }

        @Override
        public PropertyCursor rawPropertyCursor() {
            return new Cursor(pages);
        }
    }

    static final class Cursor extends MutableIntValue implements PropertyCursor {

        private final long[][] pages;
        private long[] currentPage;
        private int limit;
        private int offset;

        Cursor(long[][] pages) {
            this.pages = pages;
        }

        @Override
        public void init(long fromIndex, int degree) {
            currentPage = pages[pageIndex(fromIndex, BumpAllocator.PAGE_SHIFT)];
            offset = indexInPage(fromIndex, BumpAllocator.PAGE_MASK);
            limit = offset + degree;
        }

        @Override
        public boolean hasNextLong() {
            return offset < limit;
        }

        @Override
        public long nextLong() {
            return currentPage[offset++];
        }

        @Override
        public void close() {
        }
    }
}
