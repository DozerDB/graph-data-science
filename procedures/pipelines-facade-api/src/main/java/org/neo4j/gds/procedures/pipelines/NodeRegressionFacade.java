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
package org.neo4j.gds.procedures.pipelines;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.stream.Stream;

public interface NodeRegressionFacade {
    Stream<NodePipelineInfoResult> addLogisticRegression(
        String pipelineName,
        Map<String, Object> configuration,
        @Nullable String sessionName
    );

    default Stream<NodePipelineInfoResult> addLogisticRegression(
        String pipelineName,
        Map<String, Object> configuration
    ) {
        return addLogisticRegression(pipelineName, configuration, null);
    }

    Stream<NodePipelineInfoResult> addNodeProperty(
        String pipelineName,
        String taskName,
        Map<String, Object> procedureConfig,
        @Nullable String sessionName
    );

    default Stream<NodePipelineInfoResult> addNodeProperty(
        String pipelineName,
        String taskName,
        Map<String, Object> procedureConfig
    ) {
        return addNodeProperty(pipelineName, taskName, procedureConfig, null);
    }

    Stream<NodePipelineInfoResult> addRandomForest(
        String pipelineName,
        Map<String, Object> configuration,
        @Nullable String sessionName
    );

    default Stream<NodePipelineInfoResult> addRandomForest(
        String pipelineName,
        Map<String, Object> configuration
    ) {
        return addRandomForest(pipelineName, configuration, null);
    }

    Stream<NodePipelineInfoResult> configureAutoTuning(
        String pipelineName,
        Map<String, Object> configuration,
        @Nullable String sessionName
    );

    default Stream<NodePipelineInfoResult> configureAutoTuning(
        String pipelineName,
        Map<String, Object> configuration
    ) {
        return configureAutoTuning(pipelineName, configuration, null);
    }

    Stream<NodePipelineInfoResult> configureSplit(
        String pipelineName,
        Map<String, Object> configuration,
        @Nullable String sessionName
    );

    default Stream<NodePipelineInfoResult> configureSplit(
        String pipelineName,
        Map<String, Object> configuration
    ) {
        return configureSplit(pipelineName, configuration, null);
    }

    Stream<NodePipelineInfoResult> createPipeline(String pipelineName, @Nullable String sessionName);

    default Stream<NodePipelineInfoResult> createPipeline(String pipelineName) {
        return createPipeline(pipelineName, null);
    }

    Stream<PredictMutateResult> mutate(String graphName, Map<String, Object> configuration);

    Stream<NodeRegressionStreamResult> stream(String graphName, Map<String, Object> configuration);

    Stream<NodePipelineInfoResult> selectFeatures(
        String pipelineName,
        Object featureProperties,
        @Nullable String sessionName
    );

    default Stream<NodePipelineInfoResult> selectFeatures(
        String pipelineName,
        Object featureProperties
    ) {
        return selectFeatures(pipelineName, featureProperties, null);
    }

    Stream<NodeRegressionPipelineTrainResult> train(String graphName, Map<String, Object> configuration);

    Stream<WriteResult> write(String graphName, Map<String, Object> configuration);
}
