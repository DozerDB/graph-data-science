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
package org.neo4j.gds.procedures.community.labelpropagation;

import org.neo4j.gds.api.ProcedureReturnColumns;

import java.util.Map;

public class LabelPropagationWriteResult extends LabelPropagationStatsResult {

    public final long writeMillis;
    public final long nodePropertiesWritten;

    private LabelPropagationWriteResult(
        long ranIterations,
        boolean didConverge,
        long communityCount,
        Map<String, Object> communityDistribution,
        long preProcessingMillis,
        long computeMillis,
        long postProcessingMillis,
        long writeMillis,
        long nodePropertiesWritten,
        Map<String, Object> configuration
    ) {
        super(
            ranIterations,
            didConverge,
            communityCount,
            communityDistribution,
            preProcessingMillis,
            computeMillis,
            postProcessingMillis,
            configuration
        );
        this.writeMillis = writeMillis;
        this.nodePropertiesWritten = nodePropertiesWritten;
    }

    public static Builder builder(ProcedureReturnColumns returnColumns, int concurrency) {
        return new Builder(returnColumns, concurrency);
    }


    public static class Builder extends LabelPropagationResultBuilder<LabelPropagationWriteResult> {

        Builder(ProcedureReturnColumns returnColumns, int concurrency) {
            super(returnColumns, concurrency);
        }

        @Override
        protected LabelPropagationWriteResult buildResult() {
            return new LabelPropagationWriteResult(
                ranIterations,
                didConverge,
                maybeCommunityCount.orElse(0L),
                communityHistogramOrNull(),
                preProcessingMillis,
                computeMillis,
                postProcessingDuration,
                writeMillis,
                nodePropertiesWritten,
                config.toMap()
            );
        }
    }
}