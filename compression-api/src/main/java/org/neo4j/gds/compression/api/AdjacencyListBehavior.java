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
package org.neo4j.gds.compression.api;

import org.neo4j.gds.Aggregation;
import org.neo4j.gds.PropertyMappings;
import org.neo4j.gds.RelationshipType;
import org.neo4j.gds.mem.MemoryEstimation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.LongSupplier;
import java.util.stream.Collectors;

public final class AdjacencyListBehavior {

    private final Map<CompressionKind, AdjacencyCompressorProvider> providers;

    public AdjacencyListBehavior(List<AdjacencyCompressorProvider> providers) {
        Map<CompressionKind, AdjacencyCompressorProvider> map = new HashMap<>();
        for (AdjacencyCompressorProvider provider : providers) {
            var existing = map.put(provider.kind(), provider);
            if (existing != null) {
                throw new IllegalArgumentException(
                    "Duplicate AdjacencyCompressorProvider registered for CompressionKind '"
                    + provider.kind().name() + "': [" + existing.getClass().getName()
                    + ", " + provider.getClass().getName() + "]"
                );
            }
        }
        this.providers = Map.copyOf(map);
    }

    public static AdjacencyListBehavior fromServiceLoader() {
        var loaded = ServiceLoader.load(AdjacencyCompressorProvider.class, AdjacencyCompressorProvider.class.getClassLoader());
        var list = loaded.stream()
            .map(ServiceLoader.Provider::get)
            .collect(Collectors.toList());
        return new AdjacencyListBehavior(list);
    }

    public AdjacencyCompressorFactory get(
        CompressionKind kind,
        LongSupplier nodeCountSupplier,
        PropertyMappings propertyMappings,
        Aggregation[] aggregations
    ) {
        var provider = providerFor(kind);
        var resolved = Arrays.stream(aggregations)
            .map(Aggregation::resolve)
            .toArray(Aggregation[]::new);
        return provider.create(nodeCountSupplier, propertyMappings, resolved);
    }

    public MemoryEstimation adjacencyListEstimation(CompressionKind kind, long avgDegree, long nodeCount) {
        return providerFor(kind).adjacencyListEstimation(avgDegree, nodeCount);
    }

    public MemoryEstimation adjacencyListEstimation(CompressionKind kind, RelationshipType type, boolean undirected) {
        return providerFor(kind).adjacencyListEstimation(type, undirected);
    }

    private AdjacencyCompressorProvider providerFor(CompressionKind kind) {
        var provider = providers.get(kind);
        if (provider == null) {
            throw new IllegalStateException(
                "No AdjacencyCompressorProvider registered for CompressionKind '"
                + kind.name() + "'. Ensure the corresponding module is on the classpath."
            );
        }
        return provider;
    }
}
