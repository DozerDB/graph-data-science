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
package org.neo4j.gds.scaling.build;

import org.neo4j.gds.api.properties.nodes.NodePropertyValues;
import org.neo4j.gds.core.concurrency.Concurrency;
import org.neo4j.gds.core.concurrency.RunWithConcurrency;
import org.neo4j.gds.core.utils.partition.Partition;
import org.neo4j.gds.core.utils.partition.PartitionUtils;
import org.neo4j.gds.core.utils.progress.tasks.ProgressTracker;
import org.neo4j.gds.scaling.scale.ScalarScaler;
import org.neo4j.gds.scaling.scale.Scaler;
import org.neo4j.gds.scaling.scale.StdScore;
import org.neo4j.gds.scaling.scale.Zero;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

public final class StdBuilder {
    private StdBuilder() {}

    public static ScalarScaler create(
        NodePropertyValues properties,
        long nodeCount,
        Concurrency concurrency,
        ProgressTracker progressTracker,
        ExecutorService executor
    ) {
        var tasks = PartitionUtils.rangePartition(
            concurrency,
            nodeCount,
            partition -> new ComputeSumAndSquaredSum(partition, properties, progressTracker),
            Optional.empty()
        );
        RunWithConcurrency.builder()
            .concurrency(concurrency)
            .tasks(tasks)
            .executor(executor)
            .run();

        // calculate global metrics
        var squaredSum = tasks.stream().mapToDouble(ComputeSumAndSquaredSum::squaredSum).sum();
        var sum = tasks.stream().mapToDouble(ComputeSumAndSquaredSum::sum).sum();
        var nodeCountOmittingMissingProperties = tasks.stream().mapToLong(AggregatesComputer::nodeCountOmittingMissingValues).sum();
        var avg = sum / nodeCountOmittingMissingProperties;
        // std = σ² = Σ(pᵢ - avg)² / N =
        // (Σ(pᵢ²) + Σ(avg²) - 2avgΣ(pᵢ)) / N =
        // (Σ(pᵢ²) + Navg² - 2avgΣ(pᵢ)) / N =
        // (Σ(pᵢ²) + avg(Navg - 2Σ(pᵢ)) / N
        var variance = (squaredSum - avg * sum) / nodeCountOmittingMissingProperties;
        var std = Math.sqrt(variance);

        var statistics = Map.of(
            "avg", List.of(avg),
            "std", List.of(std)
        );

        if (std < Scaler.CLOSE_TO_ZERO) {
            return Zero.of(statistics);
        } else {
            return new StdScore(properties, statistics, avg, std);
        }
    }

    static class ComputeSumAndSquaredSum extends AggregatesComputer {

        private double squaredSum;
        private double sum;

        ComputeSumAndSquaredSum(Partition partition, NodePropertyValues property, ProgressTracker progressTracker) {
            super(partition, property, progressTracker);
            this.squaredSum = 0D;
            this.sum = 0D;
        }

        @Override
        void compute(double propertyValue) {
            this.sum += propertyValue;
            this.squaredSum += propertyValue * propertyValue;
        }

        double squaredSum() {
            return squaredSum;
        }

        double sum() {
            return sum;
        }
    }
}
