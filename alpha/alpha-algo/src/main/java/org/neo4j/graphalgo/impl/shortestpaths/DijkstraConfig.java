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
package org.neo4j.graphalgo.impl.shortestpaths;

import org.immutables.value.Value;
import org.neo4j.graphalgo.annotation.Configuration;
import org.neo4j.graphalgo.annotation.ValueClass;
import org.neo4j.graphalgo.config.AlgoBaseConfig;
import org.neo4j.graphalgo.config.GraphCreateConfig;
import org.neo4j.graphalgo.config.RelationshipWeightConfig;
import org.neo4j.graphalgo.config.WritePropertyConfig;
import org.neo4j.graphalgo.core.CypherMapWrapper;
import org.neo4j.graphdb.Node;

import java.util.Optional;

@ValueClass
@Configuration
@SuppressWarnings("immutables:subtype")
public interface DijkstraConfig extends AlgoBaseConfig, RelationshipWeightConfig, WritePropertyConfig {

    String WRITE_PROPERTY_DEFAULT_VALUE = "sssp";

    @Override
    @Value.Default
    default String writeProperty() {
        return WRITE_PROPERTY_DEFAULT_VALUE;
    }

    @Configuration.ConvertWith("nodeId")
    long startNode();

    @Configuration.ConvertWith("nodeId")
    long endNode();

    static DijkstraConfig of(long startNode, long endNode) {
        return ImmutableDijkstraConfig.builder().startNode(startNode).endNode(endNode).build();
    }

    static DijkstraConfig of(
        Optional<String> graphName,
        Optional<GraphCreateConfig> implicitCreateConfig,
        String username,
        CypherMapWrapper userInput
    ) {
        return new DijkstraConfigImpl(graphName, implicitCreateConfig, username, userInput);
    }

    static long nodeId(Node node) {
        return node.getId();
    }
}
