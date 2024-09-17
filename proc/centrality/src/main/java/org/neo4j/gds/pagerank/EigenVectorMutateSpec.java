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
package org.neo4j.gds.pagerank;

import org.neo4j.gds.NullComputationResultConsumer;
import org.neo4j.gds.executor.AlgorithmSpec;
import org.neo4j.gds.executor.ComputationResultConsumer;
import org.neo4j.gds.executor.ExecutionContext;
import org.neo4j.gds.executor.GdsCallable;
import org.neo4j.gds.procedures.algorithms.centrality.PageRankMutateResult;
import org.neo4j.gds.procedures.algorithms.configuration.NewConfigFunction;

import java.util.stream.Stream;

import static org.neo4j.gds.executor.ExecutionMode.MUTATE_NODE_PROPERTY;
import static org.neo4j.gds.pagerank.Constants.ARTICLE_RANK_DESCRIPTION;

@GdsCallable(name = "gds.eigenvector.mutate", description = ARTICLE_RANK_DESCRIPTION, executionMode = MUTATE_NODE_PROPERTY)
public class EigenVectorMutateSpec implements AlgorithmSpec<PageRankAlgorithm<EigenvectorMutateConfig>, PageRankResult, EigenvectorMutateConfig, Stream<PageRankMutateResult>, EigenvectorAlgorithmFactory<EigenvectorMutateConfig>> {

    @Override
    public String name() {
        return "EigenvectorMutate";
    }

    @Override
    public EigenvectorAlgorithmFactory<EigenvectorMutateConfig> algorithmFactory(ExecutionContext executionContext) {
        return new EigenvectorAlgorithmFactory<>();
    }
    @Override
    public NewConfigFunction<EigenvectorMutateConfig> newConfigFunction() {
        return (___, config) -> EigenvectorMutateConfig.of(config);
    }

    @Override
    public ComputationResultConsumer<PageRankAlgorithm<EigenvectorMutateConfig>, PageRankResult, EigenvectorMutateConfig, Stream<PageRankMutateResult>> computationResultConsumer() {
        return new NullComputationResultConsumer<>();
    }


}
