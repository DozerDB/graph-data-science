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

import org.junit.jupiter.api.Test;
import org.neo4j.gds.Aggregation;
import org.neo4j.gds.PropertyMappings;
import org.neo4j.gds.RelationshipType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AdjacencyListBehaviorTest {

    private static final AdjacencyCompressorProvider STUB_DVL = new AdjacencyCompressorProvider() {
        @Override public CompressionKind kind() { return CompressionKind.DELTA_VAR_LONG; }
        @Override public AdjacencyCompressorFactory create(java.util.function.LongSupplier n,
            PropertyMappings p, Aggregation[] a) { return null; }
        @Override public org.neo4j.gds.mem.MemoryEstimation adjacencyListEstimation(long avg, long nc) {
            return org.neo4j.gds.mem.MemoryEstimations.empty(); }
        @Override public org.neo4j.gds.mem.MemoryEstimation adjacencyListEstimation(
            RelationshipType t, boolean u) { return org.neo4j.gds.mem.MemoryEstimations.empty(); }
    };

    @Test
    void getReturnsFactoryForRegisteredKind() {
        var behavior = new AdjacencyListBehavior(List.of(STUB_DVL));
        var factory = behavior.get(
            CompressionKind.DELTA_VAR_LONG, () -> 10L,
            PropertyMappings.of(),
            new Aggregation[]{Aggregation.NONE}
        );
        // factory is null because stub returns null — testing dispatch, not the factory
        assertThat(factory).isNull();
    }

    @Test
    void throwsForUnregisteredKind() {
        var behavior = new AdjacencyListBehavior(List.of(STUB_DVL));
        assertThatThrownBy(() -> behavior.get(
            CompressionKind.PACKED, () -> 10L,
            PropertyMappings.of(),
            new Aggregation[]{Aggregation.NONE}
        )).isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("PACKED");
    }

    @Test
    void throwsOnDuplicateProviderRegistration() {
        assertThatThrownBy(() -> new AdjacencyListBehavior(List.of(STUB_DVL, STUB_DVL)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("DELTA_VAR_LONG");
    }

    @Test
    void estimationDelegatesToProvider() {
        var behavior = new AdjacencyListBehavior(List.of(STUB_DVL));
        var est = behavior.adjacencyListEstimation(CompressionKind.DELTA_VAR_LONG, 10L, 100L);
        assertThat(est).isNotNull();
    }
}
