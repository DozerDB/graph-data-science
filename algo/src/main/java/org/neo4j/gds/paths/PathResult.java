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
package org.neo4j.gds.paths;

import org.neo4j.gds.annotation.GenerateBuilder;

import java.util.Arrays;
import java.util.Objects;

@GenerateBuilder
public record PathResult(
    long index,
    long sourceNode,
    long targetNode,
    long[] nodeIds,
    long[] relationshipIds,
    double[] costs
) {
    public static final PathResult EMPTY = new PathResult(-1L, -1L, -1L, new long[]{-1}, new long[]{-1}, new double[]{0});
    public static PathResult withRelationshipsIds(PathResult pathResult, long[] relationshipIds) {
        return new PathResult(
            pathResult.index(),
            pathResult.sourceNode(),
            pathResult.targetNode(),
            pathResult.nodeIds(),
            relationshipIds,
            pathResult.costs()
        );
    }

    public double totalCost() {
        return costs[costs.length - 1];
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PathResult that = (PathResult) o;
        return sourceNode == that.sourceNode
            && targetNode == that.targetNode
            && Objects.deepEquals(nodeIds, that.nodeIds)
            && Objects.deepEquals(relationshipIds, that.relationshipIds)
            && Objects.deepEquals(costs, that.costs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            sourceNode,
            targetNode,
            Arrays.hashCode(nodeIds),
            Arrays.hashCode(relationshipIds),
            Arrays.hashCode(costs)
        );
    }
}
