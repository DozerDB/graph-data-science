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
package org.neo4j.gds.ml.pipeline.node.classification.predict;

import org.neo4j.gds.BaseProc;
import org.neo4j.gds.core.model.ModelCatalog;
import org.neo4j.gds.core.write.NodePropertyExporterBuilder;
import org.neo4j.gds.executor.ExecutionContext;
import org.neo4j.gds.executor.MemoryEstimationExecutor;
import org.neo4j.gds.executor.ProcedureExecutor;
import org.neo4j.gds.results.MemoryEstimateResult;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Mode;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

import java.util.Map;
import java.util.stream.Stream;

import static org.neo4j.gds.ml.pipeline.PipelineCompanion.preparePipelineConfig;
import static org.neo4j.gds.ml.pipeline.node.classification.predict.NodeClassificationPipelineConstants.ESTIMATE_PREDICT_DESCRIPTION;
import static org.neo4j.gds.ml.pipeline.node.classification.predict.NodeClassificationPipelineConstants.PREDICT_DESCRIPTION;

public class NodeClassificationPipelineWriteProc extends BaseProc {
    @Context
    public ModelCatalog internalModelCatalog;

    @Context
    public NodePropertyExporterBuilder nodePropertyExporterBuilder;

    @Procedure(name = "gds.beta.pipeline.nodeClassification.predict.write", mode = Mode.WRITE)
    @Description(PREDICT_DESCRIPTION)
    public Stream<WriteResult> write(
        @Name(value = "graphName") String graphName,
        @Name(value = "configuration", defaultValue = "{}") Map<String, Object> configuration
    ) {
        preparePipelineConfig(graphName, configuration);
        return new ProcedureExecutor<>(
            new NodeClassificationPipelineWriteSpec(),
            executionContext()
        ).compute(graphName, configuration);
    }

    @Procedure(name = "gds.beta.pipeline.nodeClassification.predict.write.estimate", mode = Mode.READ)
    @Description(ESTIMATE_PREDICT_DESCRIPTION)
    public Stream<MemoryEstimateResult> estimate(
        @Name(value = "graphNameOrConfiguration") Object graphNameOrConfiguration,
        @Name(value = "algoConfiguration") Map<String, Object> algoConfiguration
    ) {
        preparePipelineConfig(graphNameOrConfiguration, algoConfiguration);
        return new MemoryEstimationExecutor<>(
            new NodeClassificationPipelineWriteSpec(),
            executionContext(),
            transactionContext()
        ).computeEstimate(graphNameOrConfiguration, algoConfiguration);
    }

    @Override
    public ExecutionContext executionContext() {
        return super.executionContext().withNodePropertyExporterBuilder(nodePropertyExporterBuilder).withModelCatalog(internalModelCatalog);
    }
}
