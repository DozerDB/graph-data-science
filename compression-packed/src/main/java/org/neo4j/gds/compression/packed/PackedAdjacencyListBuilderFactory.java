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
package org.neo4j.gds.compression.packed;

import org.neo4j.gds.api.AdjacencyProperties;
import org.neo4j.gds.compression.api.AdjacencyListBuilder;
import org.neo4j.gds.compression.api.AdjacencyListBuilderFactory;
import org.neo4j.gds.compression.api.MemoryTracker;
import org.neo4j.gds.compression.uncompressed.UncompressedAdjacencyListBuilder;
import org.neo4j.gds.memory.access.Address;

import java.util.function.Function;

public final class PackedAdjacencyListBuilderFactory implements AdjacencyListBuilderFactory<Address, PackedAdjacencyList, long[], AdjacencyProperties> {

    private final Function<MemoryTracker, AdjacencyListBuilder<long[], AdjacencyProperties>> propertiesBuilderFactory;

    public static PackedAdjacencyListBuilderFactory of() {
        return of(UncompressedAdjacencyListBuilder::new);
    }

    /**
     * Creates a factory that delegates property builder creation to the provided function.
     * Callers supply any {@link AdjacencyListBuilder} implementation for properties.
     */
    @SuppressWarnings("unchecked")
    public static PackedAdjacencyListBuilderFactory of(
        Function<MemoryTracker, ? extends AdjacencyListBuilder<long[], ? extends AdjacencyProperties>> propertiesBuilderFactory
    ) {
        return new PackedAdjacencyListBuilderFactory(
            (Function<MemoryTracker, AdjacencyListBuilder<long[], AdjacencyProperties>>) (Function<?, ?>) propertiesBuilderFactory
        );
    }

    private PackedAdjacencyListBuilderFactory(
        Function<MemoryTracker, AdjacencyListBuilder<long[], AdjacencyProperties>> propertiesBuilderFactory
    ) {
        this.propertiesBuilderFactory = propertiesBuilderFactory;
    }

    @Override
    public PackedAdjacencyListBuilder newAdjacencyListBuilder(MemoryTracker memoryTracker) {
        return new PackedAdjacencyListBuilder(memoryTracker);
    }

    @Override
    public AdjacencyListBuilder<long[], AdjacencyProperties> newAdjacencyPropertiesBuilder(MemoryTracker memoryTracker) {
        return propertiesBuilderFactory.apply(memoryTracker);
    }
}
