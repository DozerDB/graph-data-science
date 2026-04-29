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
package org.neo4j.gds.scaling;

import org.neo4j.gds.api.properties.nodes.NodePropertyValues;
import org.neo4j.gds.core.concurrency.Concurrency;
import org.neo4j.gds.core.utils.progress.tasks.ProgressTracker;
import org.neo4j.gds.scaling.compute.CenterComputer;
import org.neo4j.gds.scaling.compute.L1NormComputer;
import org.neo4j.gds.scaling.compute.L2NormComputer;
import org.neo4j.gds.scaling.compute.MaxComputer;
import org.neo4j.gds.scaling.compute.MeanComputer;
import org.neo4j.gds.scaling.compute.MinMaxComputer;
import org.neo4j.gds.scaling.compute.StdComputer;
import org.neo4j.gds.scaling.scale.LogScaler;
import org.neo4j.gds.scaling.scale.NoneScaler;
import org.neo4j.gds.scaling.scale.ScalarScaler;
import org.neo4j.gds.scaling.scale.Zero;

import java.util.Locale;
import java.util.concurrent.ExecutorService;

public final class ScalerFactory {
    private final ScalerType type;
    private final String name;
    private final double offset;

    private ScalerFactory(ScalerType type, String name, double offset) {
        this.type = type;
        this.name = name;
        this.offset = offset;
    }

    public static ScalerFactory of(ScalerType type, String name, double offset) {
        return new ScalerFactory(type, name, offset);
    }

    public static ScalerFactory of(ScalerType type, double offset) {
        return new ScalerFactory(type, type.scalerName(), offset);
    }

    public static ScalerFactory of(ScalerType type) {
        return of(type, 0.0);
    }

    public String name() { return name; }

    public ScalerType type() { return type; }

    public ScalarScaler create(
        NodePropertyValues properties,
        long nodeCount,
        Concurrency concurrency,
        ProgressTracker progressTracker,
        ExecutorService executor
    ) {
        return switch (type) {
            case None -> NoneScaler.of(properties);
            case Zero -> Zero.of();
            case Log -> LogScaler.of(properties, offset);
            case Center -> CenterComputer.create(
                properties,
                nodeCount,
                concurrency,
                progressTracker,
                executor);
            case Mean -> MeanComputer.create(
                properties,
                nodeCount,
                concurrency,
                progressTracker,
                executor);
            case Max -> MaxComputer.create(
                properties,
                nodeCount,
                concurrency,
                progressTracker,
                executor);
            case MinMax -> MinMaxComputer.create(
                properties,
                nodeCount,
                concurrency,
                progressTracker,
                executor);
            case L1Norm -> L1NormComputer.create(
                properties,
                nodeCount,
                concurrency,
                progressTracker,
                executor);
            case L2Norm -> L2NormComputer.create(
                properties,
                nodeCount,
                concurrency,
                progressTracker,
                executor);
            case Std -> StdComputer.create(
                properties,
                nodeCount,
                concurrency,
                progressTracker,
                executor);
        };
    }

    public boolean workingScaler() { return type != ScalerType.None; }

    public static String toString(ScalerFactory factory) {
        return factory.name().toUpperCase(Locale.ENGLISH);
    }
}
