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
package org.neo4j.gds.compression.varlong;

import org.neo4j.gds.Aggregation;
import org.neo4j.gds.PropertyMappings;
import org.neo4j.gds.RelationshipType;
import org.neo4j.gds.compression.api.AdjacencyCompressorFactory;
import org.neo4j.gds.compression.api.AdjacencyCompressorProvider;
import org.neo4j.gds.compression.api.CompressionKind;
import org.neo4j.gds.compression.api.MemoryTracker;
import org.neo4j.gds.compression.uncompressed.UncompressedAdjacencyListBuilder;
import org.neo4j.gds.mem.MemoryEstimation;

import java.util.Arrays;
import java.util.function.LongSupplier;

public final class DeltaVarLongCompressorProvider implements AdjacencyCompressorProvider {

    @Override
    public CompressionKind kind() {
        return CompressionKind.DELTA_VAR_LONG;
    }

    @Override
    public AdjacencyCompressorFactory create(
        LongSupplier nodeCountSupplier,
        PropertyMappings propertyMappings,
        Aggregation[] aggregations
    ) {
        boolean noAggregation = Arrays.stream(aggregations)
            .map(Aggregation::resolve)
            .allMatch(Aggregation::equivalentToNone);
        return DeltaVarLongCompressor.factory(
            nodeCountSupplier,
            CompressedAdjacencyListBuilderFactory.of(UncompressedAdjacencyListBuilder::new),
            propertyMappings,
            aggregations,
            noAggregation,
            MemoryTracker.create()
        );
    }

    @Override
    public MemoryEstimation adjacencyListEstimation(long avgDegree, long nodeCount) {
        return CompressedAdjacencyList.adjacencyListEstimation(avgDegree, nodeCount);
    }

    @Override
    public MemoryEstimation adjacencyListEstimation(RelationshipType type, boolean undirected) {
        return CompressedAdjacencyList.adjacencyListEstimation(type, undirected);
    }
}
