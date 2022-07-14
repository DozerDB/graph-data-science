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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.neo4j.gds.NodeLabel;
import org.neo4j.gds.RelationshipType;
import org.neo4j.gds.api.Graph;
import org.neo4j.gds.api.IdMap;
import org.neo4j.gds.api.nodeproperties.ValueType;
import org.neo4j.gds.api.properties.graph.LongGraphPropertyValues;
import org.neo4j.gds.api.schema.PropertySchema;
import org.neo4j.gds.gdl.GdlFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.neo4j.gds.TestSupport.assertGraphEquals;
import static org.neo4j.gds.TestSupport.fromGdl;

class CSRGraphStoreTest {

    @Test
    void deleteAdditionalRelationshipTypes() {
        GdlFactory factory = GdlFactory.of("(b)-[:REL {x: 1}]->(a), (b)-[:REL]->(c)");
        var graphStore = factory.build();

        var del1 = graphStore.deleteRelationships(RelationshipType.of("REL"));

        assertThat(del1.deletedRelationships()).isEqualTo(2);
        assertThat(del1.deletedProperties()).containsEntry("x", 2L).hasSize(1);

        var del2 = graphStore.deleteRelationships(RelationshipType.of("REL"));

        assertThat(del2.deletedRelationships()).isEqualTo(0);
        assertThat(del2.deletedProperties()).isEmpty();
    }

    @Test
    void validateRelationshipTypesWhenNoneExist() {
        GdlFactory factory = GdlFactory.of("(a), (b)");
        var graphStore = factory.build();

        assertThatThrownBy(() -> graphStore.getGraph(
            List.of(NodeLabel.ALL_NODES),
            List.of(RelationshipType.of("X")),
            Optional.empty()
        )).hasMessageContaining("No relationships have been loaded for relationship type").hasMessageContaining("X");
    }

    @Test
    void gettingGraphsWithRelationshipTypes() {
        GdlFactory factory = GdlFactory.of("()-[:T]->()-[:R]->()-[:R]->()");
        var graphStore = factory.build();

        var t_graph = graphStore.getGraph(graphStore.nodeLabels(), List.of(RelationshipType.of("T")), Optional.empty());
        var r_graph = graphStore.getGraph(graphStore.nodeLabels(), List.of(RelationshipType.of("R")), Optional.empty());
        var t_r_graph = graphStore.getGraph(
            graphStore.nodeLabels(),
            List.of(RelationshipType.of("R"), RelationshipType.of("T")),
            Optional.empty()
        );
        var none_graph = graphStore.getGraph(graphStore.nodeLabels(), List.of(), Optional.empty());

        assertGraphEquals(fromGdl("()-[:T]->(), (), ()"), t_graph);
        assertGraphEquals(fromGdl("(), ()-[:R]->()-[:R]->()"), r_graph);
        assertGraphEquals(fromGdl("()-[:T]->()-[:R]->()-[:R]->()"), t_r_graph);
        assertGraphEquals(fromGdl("(), (), (), ()"), none_graph);
    }

    @Test
    void shouldPropagateLabelFilterToNodeSchema() {
        var factory = GdlFactory.of("(:A), (:A), (:B), (:C)");
        var graphStore = factory.build();

        assertThat(graphStore.schema().nodeSchema().availableLabels())
            .containsExactlyInAnyOrder(NodeLabel.of("A"), NodeLabel.of("B"), NodeLabel.of("C"));

        var graph = graphStore.getGraph(Set.of(NodeLabel.of("B"), NodeLabel.of("C")));
        assertThat(graph.schema().nodeSchema().availableLabels())
            .containsExactlyInAnyOrder(NodeLabel.of("B"), NodeLabel.of("C"));
    }

    @Test
    void shouldAddNewGraphProperties() {
        var factory = GdlFactory.of("(:A), (:A), (:B), (:C)");
        var graphStore = factory.build();

        graphStore.addGraphProperty("longProp", new LongGraphPropertyValues() {
            @Override
            public LongStream longValues() {
                return LongStream.range(0, 4);
            }

            @Override
            public long size() {
                return 4;
            }
        });

        assertThat(graphStore.graphProperty("longProp")).isNotNull();
        assertThat(graphStore.schema().graphProperties()).containsEntry(
            "longProp",
            PropertySchema.of("longProp", ValueType.LONG)
        );
    }

    @ParameterizedTest
    @MethodSource("nthTestGraphs")
    void nthTargetIsConsistentWithRelationshipIteration(Graph graph) {
        var expected = new HashMap<Long, List<Long>>();
        var actual = new HashMap<Long, List<Long>>();
        for (long nodeId = 0; nodeId < graph.nodeCount(); nodeId++) {
            var expectedNeighbors = new ArrayList<Long>();
            expected.put(nodeId, expectedNeighbors);
            graph.forEachRelationship(nodeId, (src, trg) -> {
                expectedNeighbors.add(trg);
                return true;
            });

            var actualNeighbors = new ArrayList<Long>();
            actual.put(nodeId, actualNeighbors);
            for (int offset = 0; offset < graph.degree(nodeId); offset++) {
                actualNeighbors.add(graph.nthTarget(nodeId, offset));
            }
        }
        assertThat(actual).isEqualTo(expected);
    }

    static Stream<Graph> nthTestGraphs() {
        return Stream.of(
            GdlFactory.of(
                " (a)-[:T]->(b)" +
                ", (b)-[:T]->(a)" +
                ", (b)-[:T]->(c)" +
                ", (a)-[:T]->(b)" +
                ", (b)-[:T]->(a)" +
                ", (b)-[:T]->(b)" +
                ", (b)-[:T]->(c)"
            ).build().getUnion(),
            GdlFactory.of(
                " (a)-[:T]->(b)" +
                ", (b)-[:A]->(a)" +
                ", (b)-[:B]->(c)" +
                ", (a)-[:A]->(b)" +
                ", (b)-[:A]->(a)" +
                ", (b)-[:B]->(b)" +
                ", (b)-[:T]->(c)"
            ).build().getUnion()
        );
    }

    @Test
    void outOfBoundNthTargetShouldReturnNotFound() {
        var factory = GdlFactory.of(" (a)-[:T]->(b)");
        var graphStore = factory.build();
        var graph = graphStore.getUnion();

        assertThat(graph.nthTarget(0, 1)).isEqualTo(IdMap.NOT_FOUND);
        assertThat(graph.nthTarget(1, 0)).isEqualTo(IdMap.NOT_FOUND);
    }
}
