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
package org.neo4j.gds.ml.pipeline;

import org.neo4j.gds.ElementIdentifier;
import org.neo4j.gds.NodeLabel;
import org.neo4j.gds.RelationshipType;
import org.neo4j.gds.pipeline.ExecutionContext;
import org.neo4j.gds.pipeline.GdsCallableFinder;
import org.neo4j.gds.pipeline.ProcedureExecutor;
import org.neo4j.gds.pipeline.ProcedurePipelineSpec;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public final class NodePropertyStep implements ExecutableNodePropertyStep {
    private final GdsCallableFinder.GdsCallableDefinition callableDefinition;
    private final Map<String, Object> config;

    public NodePropertyStep(
        GdsCallableFinder.GdsCallableDefinition callableDefinition,
        // TODO: pass in the config instead
        Map<String, Object> config
    ) {
        this.callableDefinition = callableDefinition;
        this.config = config;
    }

    @Override
    // TODO make this return the actual configuration
    public Map<String, Object> config() {
        return config;
    }

    @Override
    public String procName() {
        return callableDefinition.name();
    }

    @Override
    public void execute(
        ExecutionContext executionContext,
        String graphName,
        Collection<NodeLabel> nodeLabels,
        Collection<RelationshipType> relTypes
    ) {
        var configCopy = new HashMap<>(config);
        var nodeLabelStrings = nodeLabels.stream().map(ElementIdentifier::name).collect(Collectors.toList());
        var relTypeStrings = relTypes.stream().map(ElementIdentifier::name).collect(Collectors.toList());
        configCopy.put("nodeLabels", nodeLabelStrings);
        configCopy.put("relationshipTypes", relTypeStrings);

        new ProcedureExecutor<>(
            callableDefinition.algorithmSpec(),
            new ProcedurePipelineSpec<>(),
            executionContext
        ).compute(graphName, configCopy, false, false);
    }

    @Override
    public Map<String, Object> toMap() {
        return Map.of("name", procName(), "config", config);
    }
}
