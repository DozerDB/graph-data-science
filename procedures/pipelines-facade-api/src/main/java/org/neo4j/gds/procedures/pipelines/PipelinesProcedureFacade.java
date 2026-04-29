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

import java.util.stream.Stream;

public interface PipelinesProcedureFacade {
    String NO_VALUE = "__NO_VALUE";

    Stream<PipelineCatalogResult> dropPipeline(
        String pipelineNameAsString,
        boolean failIfMissing,
        @Nullable String sessionName
    );

    Stream<PipelineExistsResult> existsPipeline(String pipelineNameAsString, @Nullable String sessionName);

    Stream<PipelineCatalogResult> listPipelines(String pipelineNameAsString, @Nullable String sessionName);

    LinkPredictionFacade linkPrediction();

    NodeClassificationFacade nodeClassification();

    NodeRegressionFacade nodeRegression();
}
