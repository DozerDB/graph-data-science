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

import org.neo4j.gds.api.nodeproperties.ValueType;
import org.neo4j.gds.beta.pregel.Element;
import org.neo4j.gds.beta.pregel.Messages;
import org.neo4j.gds.beta.pregel.PregelComputation;
import org.neo4j.gds.beta.pregel.PregelConfig;
import org.neo4j.gds.beta.pregel.PregelSchema;
import org.neo4j.gds.beta.pregel.Reducer;
import org.neo4j.gds.beta.pregel.context.ComputeContext;
import org.neo4j.gds.beta.pregel.context.InitContext;
import org.neo4j.gds.mem.MemoryEstimateDefinition;

import java.util.Optional;
import java.util.function.LongToDoubleFunction;

public final class ArticleRankComputation implements PregelComputation<PregelConfig> {

    private static final String PAGE_RANK = "pagerank";

    private final InitialProbabilityProvider initialProbability;
    private final LongToDoubleFunction degreeFunction;

    private final double dampingFactor;
    private final double tolerance;
    private final double averageDegree;

    public ArticleRankComputation(
        ArticleRankConfig config,
        InitialProbabilityProvider initialProbabilityProvider,
        LongToDoubleFunction degreeFunction,
        double averageDegree
    ) {
        this.dampingFactor = config.dampingFactor();
        this.tolerance = config.tolerance();
        this.averageDegree = averageDegree;
        this.initialProbability = initialProbabilityProvider;
        this.degreeFunction = degreeFunction;
    }

    @Override
    public PregelSchema schema(PregelConfig config) {
        return PregelSchema.from(new Element(PAGE_RANK, ValueType.DOUBLE));
    }

    @Override
    public void init(InitContext<PregelConfig> context) {
        context.setNodeValue(PAGE_RANK, initialValue(context));
    }

    private double initialValue(InitContext<PregelConfig> context) {
        return initialProbability.provideInitialValue(context.nodeId());
    }

    @Override
    public void compute(ComputeContext<PregelConfig> context, Messages messages) {
        double rank = context.doubleNodeValue(PAGE_RANK);
        double delta = rank;

        if (!context.isInitialSuperstep()) {
            double sum = 0;
            for (var message : messages) {
                sum += message;
            }
            delta = dampingFactor * sum;
            context.setNodeValue(PAGE_RANK, rank + delta);
        }

        if (delta > tolerance || context.isInitialSuperstep()) {
            var degree = degreeFunction.applyAsDouble(context.nodeId());
            if (degree > 0) {
                // different from the original ArticleRank paper as we use deltas instead of the whole rank
                // to avoid exploding scores, we use `1 / (degree + avgDegree)`
                // instead of the proposed `avgDegree / (degree + avgDegree)`
                context.sendToNeighbors(delta / (degree + averageDegree));
            }
        } else {
            context.voteToHalt();
        }
    }

    @Override
    public Optional<Reducer> reducer() {
        return Optional.of(new Reducer.Sum());
    }

    @Override
    public double applyRelationshipWeight(double nodeValue, double relationshipWeight) {
        return nodeValue * relationshipWeight;
    }

    @Override
    public MemoryEstimateDefinition estimateDefinition(boolean isAsynchronous) {
        return new PageRankMemoryEstimateDefinition();
    }
}
