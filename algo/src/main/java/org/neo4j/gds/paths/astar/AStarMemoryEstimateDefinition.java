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
package org.neo4j.gds.paths.astar;

import org.jetbrains.annotations.Nullable;
import org.neo4j.gds.AlgorithmMemoryEstimateDefinition;
import org.neo4j.gds.core.utils.mem.MemoryEstimation;
import org.neo4j.gds.core.utils.mem.MemoryEstimations;
import org.neo4j.gds.core.utils.paged.HugeLongDoubleMap;
import org.neo4j.gds.paths.dijkstra.DijkstraMemoryEstimateDefinition;
import org.neo4j.gds.paths.dijkstra.DijkstraMemoryEstimateParameters;

public class AStarMemoryEstimateDefinition implements AlgorithmMemoryEstimateDefinition<Void> {

    @Override
    public MemoryEstimation memoryEstimation(@Nullable Void unusedEvilParameter) {

        return MemoryEstimations.builder(AStar.class)
            .add(
                "Dijkstra",
                new DijkstraMemoryEstimateDefinition()
                    .memoryEstimation(new DijkstraMemoryEstimateParameters(false, false))
            )
            .add("distanceCache", HugeLongDoubleMap.memoryEstimation())
            .build();

    }

}
