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
package org.neo4j.gds.similarity.knn;

import org.neo4j.gds.procedures.similarity.knn.KnnWriteResult;
import org.neo4j.gds.similarity.SimilarityResultBuilder;

public class KnnWriteResultBuilder extends SimilarityResultBuilder<KnnWriteResult> {
        private long ranIterations;
        private boolean didConverge;
        private long nodePairsConsidered;

        @Override
        public KnnWriteResult build() {
            return new KnnWriteResult(
                preProcessingMillis,
                computeMillis,
                writeMillis,
                postProcessingMillis,
                nodesCompared,
                relationshipsWritten,
                didConverge,
                ranIterations,
                nodePairsConsidered,
                distribution(),
                config.toMap()
            );
        }

        public KnnWriteResultBuilder withDidConverge(boolean didConverge) {
            this.didConverge = didConverge;
            return this;
        }

        public KnnWriteResultBuilder withRanIterations(long ranIterations) {
            this.ranIterations = ranIterations;
            return this;
        }

    public KnnWriteResultBuilder withNodePairsConsidered(long nodePairsConsidered) {
            this.nodePairsConsidered = nodePairsConsidered;
            return this;
        }
    }

