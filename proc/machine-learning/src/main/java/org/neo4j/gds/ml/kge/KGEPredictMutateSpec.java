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
package org.neo4j.gds.ml.kge;

import org.neo4j.gds.algorithms.machinelearning.KGEPredictMutateConfig;
import org.neo4j.gds.algorithms.machinelearning.KGEPredictResult;
import org.neo4j.gds.algorithms.machinelearning.TopKMapComputer;
import org.neo4j.gds.executor.AlgorithmSpec;
import org.neo4j.gds.executor.ComputationResult;
import org.neo4j.gds.executor.ComputationResultConsumer;
import org.neo4j.gds.executor.ExecutionContext;
import org.neo4j.gds.executor.GdsCallable;
import org.neo4j.gds.procedures.algorithms.configuration.NewConfigFunction;
import org.neo4j.gds.procedures.algorithms.machinelearning.KGEMutateResult;

import java.util.stream.Stream;

import static org.neo4j.gds.executor.ExecutionMode.MUTATE_RELATIONSHIP;

@GdsCallable(name = "gds.ml.kge.predict.mutate", description = "Predicts new relationships using an existing KGE model", executionMode = MUTATE_RELATIONSHIP)
public class KGEPredictMutateSpec implements AlgorithmSpec<
    TopKMapComputer,
    KGEPredictResult,
    KGEPredictMutateConfig,
    Stream<KGEMutateResult>,
    KGEPredictAlgorithmFactory<KGEPredictMutateConfig>> {
    @Override
    public String name() {
        return "KGEPredictMutate";
    }

    @Override
    public KGEPredictAlgorithmFactory<KGEPredictMutateConfig> algorithmFactory(ExecutionContext executionContext) {
        return new KGEPredictAlgorithmFactory<>();
    }

    @Override
    public NewConfigFunction<KGEPredictMutateConfig> newConfigFunction() {
        return (__, config) -> KGEPredictMutateConfig.of(config);
    }

    @Override
    public ComputationResultConsumer<TopKMapComputer, KGEPredictResult, KGEPredictMutateConfig, Stream<KGEMutateResult>> computationResultConsumer() {
        return new KGEMutateResultConsumer(this::resultBuilder);
    }

    private KGEMutateResult.Builder resultBuilder(
        ComputationResult<TopKMapComputer, KGEPredictResult, KGEPredictMutateConfig> computeResult,
        ExecutionContext executionContext
    ) {
        return new KGEMutateResult.Builder();
    }
}
