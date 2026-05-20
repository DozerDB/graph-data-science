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

import org.neo4j.gds.GraphParameters;
import org.neo4j.gds.RelationshipType;
import org.neo4j.gds.api.Graph;
import org.neo4j.gds.api.GraphStore;
import org.neo4j.gds.core.DimensionsMap;
import org.neo4j.gds.core.GraphDimensions;
import org.neo4j.gds.core.ImmutableGraphDimensions;
import org.neo4j.gds.core.loading.GraphResources;
import org.neo4j.gds.core.loading.GraphStoreCatalogService;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class GraphDimensionsFactory {

    public GraphDimensionsFactory() {}

    private  Map<RelationshipType, Long> filteredGraphRelationshipCounts(
        Stream<RelationshipType> typeFilter,
        Graph filteredGraph
    ) {
        var relCount = filteredGraph.relationshipCount();
        return Stream.concat(typeFilter, Stream.of(RelationshipType.ALL_RELATIONSHIPS))
            .distinct()
            .collect(Collectors.toMap(
                    Function.identity(),
                    key -> key == RelationshipType.ALL_RELATIONSHIPS
                        ? relCount
                        : filteredGraph
                            .relationshipTypeFilteredGraph(Set.of(key))
                            .relationshipCount()
                )
            );
    }

    public GraphDimensions graphDimensions(GraphStore graphStore, Graph graph, Collection<RelationshipType> relationshipTypesFilter) {

        long relCount =  graph.relationshipCount();
        var relationshipTypeTokens = new HashMap<String, Integer>();
        var i = 0;
        for (String key : graphStore.relationshipPropertyKeys()) {
            relationshipTypeTokens.put(key, i++);
        }

        var nodePropertyDimensions = graph
            .availableNodeProperties()
            .stream()
            .collect(Collectors.toMap(
                Function.identity(),
                property -> graph
                    .nodeProperties(property)
                    .dimension()
            ));

        return ImmutableGraphDimensions.builder()
            .nodeCount(graph.nodeCount())
            .relationshipCounts(filteredGraphRelationshipCounts(relationshipTypesFilter.stream(), graph))
            .relCountUpperBound(relCount)
            .relationshipPropertyTokens(relationshipTypeTokens)
            .nodePropertyDimensions(new DimensionsMap(nodePropertyDimensions))
            .build();
    }

    public GraphDimensions graphDimensions(GraphResources graphResources, GraphParameters graphParameters){
        return graphDimensions(graphResources.graphStore(), graphResources.graph(), graphParameters.relationshipTypesFilter());
    }

    public GraphDimensions graphDimensions(GraphStore graphStore, GraphParameters graphParameters){

        var resolveNodeLabels = GraphStoreCatalogService.resolveNodeLabels(graphStore, graphParameters.nodeLabelsFilter());
        var resolvedRelationshipTypes = GraphStoreCatalogService.resolveRelationshipTypes(graphStore, graphParameters.loadAllRelationshipTypes(), graphParameters.relationshipTypesFilter());

        var filteredGraph = graphStore.getGraph(resolveNodeLabels, resolvedRelationshipTypes, Optional.empty());

        return graphDimensions(graphStore,filteredGraph, graphParameters.relationshipTypesFilter());
    }
}
