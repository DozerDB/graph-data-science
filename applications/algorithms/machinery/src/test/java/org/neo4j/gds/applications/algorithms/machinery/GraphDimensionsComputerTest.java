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
package org.neo4j.gds.applications.algorithms.machinery;

import org.junit.jupiter.api.Test;
import org.neo4j.gds.GraphParameters;
import org.neo4j.gds.RelationshipType;
import org.neo4j.gds.api.Graph;
import org.neo4j.gds.api.GraphStore;
import org.neo4j.gds.api.properties.nodes.NodePropertyValues;
import org.neo4j.gds.core.GraphDimensions;
import org.neo4j.gds.core.loading.GraphResources;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GraphDimensionsComputerTest {

    @Test
    void shouldComputeDimensions() {

        var graphStore = mock(GraphStore.class);
        var graph = mock(Graph.class);

        RelationshipType type1 = RelationshipType.of("KNOWS");
        RelationshipType type2 = RelationshipType.of("ALSO-KNOWS");

        String propertyKey1 = "age";
        String propertyKey2 = "age-2";

        GraphParameters parameters = new GraphParameters(
            Set.of(),
            Set.of(type1,type2),
            false,
            Optional.empty()
        );

        when(graph.nodeCount()).thenReturn(100L);
        when(graph.relationshipCount()).thenReturn(200L);
        when(graph.availableNodeProperties()).thenReturn(Set.of(propertyKey1,propertyKey2));

        NodePropertyValues propertyValues1 = mock(NodePropertyValues.class);
        NodePropertyValues propertyValues2 = mock(NodePropertyValues.class);

        when(propertyValues1.dimension()).thenReturn(Optional.of(150));
        when(propertyValues2.dimension()).thenReturn(Optional.of(50));

        when(graph.nodeProperties(propertyKey1)).thenReturn(propertyValues1);
        when(graph.nodeProperties(propertyKey2)).thenReturn(propertyValues2);


        Graph filteredTypeGraph = mock(Graph.class);
        when(filteredTypeGraph.relationshipCount()).thenReturn(150L);
        when(graph.relationshipTypeFilteredGraph(Set.of(type1))).thenReturn(filteredTypeGraph);
        when(graph.relationshipTypeFilteredGraph(Set.of(type2))).thenReturn(filteredTypeGraph);

        when(graphStore.relationshipPropertyKeys()).thenReturn(Set.of("weight","also-weight"));

        GraphResources resources = new GraphResources(graphStore, graph, null);

        GraphDimensions dimensions = GraphDimensionsComputer.of(parameters, resources);

        assertThat(dimensions.nodeCount()).isEqualTo(100L);
        assertThat(dimensions.relCountUpperBound()).isEqualTo(200L);
        assertThat(dimensions.relationshipCounts()).containsEntry(type1, 150L);
        assertThat(dimensions.relationshipCounts()).containsEntry(type2, 150L);
        assertThat(dimensions.relationshipCounts()).containsEntry(RelationshipType.ALL_RELATIONSHIPS, 200L);

        assertThat(dimensions.relationshipPropertyTokens()).containsKeys("weight", "also-weight");
        assertThat(dimensions.relationshipPropertyTokens()).containsValues(0, 1);

        assertThat(dimensions.nodePropertyDimensions().get(propertyKey1).orElseThrow()).isEqualTo(150);
        assertThat(dimensions.nodePropertyDimensions().get(propertyKey2).orElseThrow()).isEqualTo(50);

    }
}
