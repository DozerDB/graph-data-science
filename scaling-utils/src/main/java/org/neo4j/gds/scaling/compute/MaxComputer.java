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
package org.neo4j.gds.scaling.compute;

import org.neo4j.gds.api.properties.nodes.NodePropertyValues;
import org.neo4j.gds.core.concurrency.Concurrency;
import org.neo4j.gds.core.concurrency.RunWithConcurrency;
import org.neo4j.gds.core.utils.partition.Partition;
import org.neo4j.gds.core.utils.partition.PartitionUtils;
import org.neo4j.gds.core.utils.progress.tasks.ProgressTracker;
import org.neo4j.gds.scaling.scale.Max;
import org.neo4j.gds.scaling.scale.ScalarScaler;
import org.neo4j.gds.scaling.scale.Scaler;
import org.neo4j.gds.scaling.scale.Zero;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

public final class MaxComputer {
    private MaxComputer() {}

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
            partition -> new ComputeAbsMax(partition, properties, progressTracker),
            Optional.empty()
        );
        RunWithConcurrency.builder()
            .concurrency(concurrency)
            .tasks(tasks)
            .executor(executor)
            .run();

        var absMax = tasks.stream().mapToDouble(ComputeAbsMax::absMax).max().orElse(0);

        var statistics = Map.of("absMax", List.of(absMax));

        if (absMax < Scaler.CLOSE_TO_ZERO) {
            return Zero.of(statistics);
        } else {
            return new Max(properties, statistics, absMax);
        }
    }

    static class ComputeAbsMax extends AggregatesComputer {

        private double absMax;

        ComputeAbsMax(Partition partition, NodePropertyValues property, ProgressTracker progressTracker) {
            super(partition, property, progressTracker);
            this.absMax = 0;
        }

        @Override
        void compute(double propertyValue) {
            var absoluteValue = Math.abs(propertyValue);
            if (absoluteValue > absMax) {
                absMax = absoluteValue;
            }
        }

        double absMax() {
            return absMax;
        }
    }
}
