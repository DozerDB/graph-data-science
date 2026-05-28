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
package org.neo4j.gds.core.loading;

import org.neo4j.gds.PropertyMappings;
import org.neo4j.gds.RelationshipType;
import org.neo4j.gds.compression.api.AdjacencyCompressorFactory;
import org.neo4j.gds.compression.api.CompressionKind;
import org.neo4j.gds.compression.uncompressed.UncompressedAdjacencyList;
import org.neo4j.gds.mem.MemoryEstimation;
import org.neo4j.gds.mem.MemoryEstimations;
import org.neo4j.gds.Aggregation;
import org.neo4j.gds.utils.GdsFeatureToggles;

import java.util.function.LongSupplier;

import static org.neo4j.gds.RelationshipType.ALL_RELATIONSHIPS;

/**
 * Manages different configurations of adjacency list building,
 * i.e., compressed or uncompressed.
 */
public final class AdjacencyListBehavior {

    private static final org.neo4j.gds.compression.api.AdjacencyListBehavior BEHAVIOR =
        org.neo4j.gds.compression.api.AdjacencyListBehavior.fromServiceLoader();

    private AdjacencyListBehavior() {}

    @FunctionalInterface
    public interface Factory {
        AdjacencyCompressorFactory create(
            LongSupplier nodeCountSupplier,
            PropertyMappings propertyMappings,
            Aggregation[] aggregations
        );
    }

    private static CompressionKind resolveKind() {
        if (GdsFeatureToggles.USE_PACKED_ADJACENCY_LIST.isEnabled())       return CompressionKind.PACKED;
        if (GdsFeatureToggles.USE_MIXED_ADJACENCY_LIST.isEnabled())        return CompressionKind.MIXED;
        if (GdsFeatureToggles.USE_UNCOMPRESSED_ADJACENCY_LIST.isEnabled()) return CompressionKind.UNCOMPRESSED;
        return CompressionKind.DELTA_VAR_LONG;
    }

    public static AdjacencyCompressorFactory asConfigured(
        LongSupplier nodeCountSupplier,
        PropertyMappings propertyMappings,
        Aggregation[] aggregations
    ) {
        return BEHAVIOR.get(resolveKind(), nodeCountSupplier, propertyMappings, aggregations);
    }

    public static MemoryEstimation adjacencyListEstimation(long avgDegree, long nodeCount) {
        return BEHAVIOR.adjacencyListEstimation(resolveKind(), avgDegree, nodeCount);
    }

    public static MemoryEstimation adjacencyListEstimation(RelationshipType relationshipType, boolean undirected) {
        return BEHAVIOR.adjacencyListEstimation(resolveKind(), relationshipType, undirected);
    }

    public static MemoryEstimation adjacencyListsFromStarEstimation(boolean undirected) {
        return MemoryEstimations.setup("Adjacency Lists", dimensions -> {
            var builder = MemoryEstimations.builder();
            if (dimensions.relationshipCounts().isEmpty()) {
                builder.add(adjacencyListEstimation(ALL_RELATIONSHIPS, undirected));
            } else {
                dimensions.relationshipCounts()
                    .forEach((type, count) -> builder.add(type.name, adjacencyListEstimation(type, undirected)));
            }
            return builder.build();
        });
    }

    public static MemoryEstimation adjacencyPropertiesEstimation(RelationshipType relationshipType, boolean undirected) {
        return UncompressedAdjacencyList.adjacencyPropertiesEstimation(relationshipType, undirected);
    }

    public static MemoryEstimation adjacencyPropertiesFromStarEstimation(boolean undirected) {
        return MemoryEstimations.setup("", dimensions -> {
            var builder = MemoryEstimations.builder();
            if (dimensions.relationshipCounts().isEmpty()) {
                builder.add(UncompressedAdjacencyList.adjacencyPropertiesEstimation(ALL_RELATIONSHIPS, undirected));
            } else {
                dimensions.relationshipCounts()
                    .forEach((type, count) -> builder.add(
                        type.name,
                        UncompressedAdjacencyList.adjacencyPropertiesEstimation(type, undirected)
                    ));
            }
            return builder.build();
        });
    }
}
