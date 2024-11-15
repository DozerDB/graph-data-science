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
package org.neo4j.gds.similarity.filteredknn;

import org.neo4j.gds.NullComputationResultConsumer;
import org.neo4j.gds.executor.AlgorithmSpec;
import org.neo4j.gds.executor.ComputationResultConsumer;
import org.neo4j.gds.executor.ExecutionContext;
import org.neo4j.gds.executor.GdsCallable;
import org.neo4j.gds.procedures.algorithms.configuration.NewConfigFunction;
import org.neo4j.gds.procedures.algorithms.similarity.KnnWriteResult;

import java.util.stream.Stream;

import static org.neo4j.gds.executor.ExecutionMode.WRITE_RELATIONSHIP;

@GdsCallable(
    name = "gds.knn.filtered.write",
    aliases = {"gds.alpha.knn.filtered.write"},
    description = Constants.FILTERED_KNN_DESCRIPTION,
    executionMode = WRITE_RELATIONSHIP
)
public class FilteredKnnWriteSpecification implements AlgorithmSpec<FilteredKnn, FilteredKnnResult, FilteredKnnWriteConfig, Stream<KnnWriteResult>, FilteredKnnFactory<FilteredKnnWriteConfig>> {
    @Override
    public String name() {
        return "FilteredKnnWrite";
    }

    @Override
    public FilteredKnnFactory<FilteredKnnWriteConfig> algorithmFactory(ExecutionContext executionContext) {
        return new FilteredKnnFactory<>();
    }

    @Override
    public NewConfigFunction<FilteredKnnWriteConfig> newConfigFunction() {
        return (__, userInput) -> FilteredKnnWriteConfig.of(userInput);
    }

    @Override
    public ComputationResultConsumer<FilteredKnn, FilteredKnnResult, FilteredKnnWriteConfig, Stream<KnnWriteResult>> computationResultConsumer() {
        return new NullComputationResultConsumer<>();
    }
}
