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
package org.neo4j.gds.applications.algorithms.similarity;

import org.apache.commons.lang3.tuple.Pair;
import org.neo4j.gds.algorithms.similarity.MutateRelationshipService;
import org.neo4j.gds.algorithms.similarity.SimilarityResultCompanion;
import org.neo4j.gds.algorithms.similarity.SimilaritySingleTypeRelationshipsHandler;
import org.neo4j.gds.api.Graph;
import org.neo4j.gds.api.GraphStore;
import org.neo4j.gds.applications.algorithms.machinery.MutateOrWriteStep;
import org.neo4j.gds.applications.algorithms.metadata.RelationshipsWritten;
import org.neo4j.gds.logging.Log;
import org.neo4j.gds.similarity.knn.KnnMutateConfig;
import org.neo4j.gds.similarity.knn.KnnResult;

import java.util.Map;

class KnnMutateStep implements MutateOrWriteStep<KnnResult, Pair<RelationshipsWritten, Map<String, Object>>> {
    private final Log log;
    private final KnnMutateConfig configuration;
    private final boolean shouldComputeSimilarityDistribution;

    KnnMutateStep(Log log, KnnMutateConfig configuration, boolean shouldComputeSimilarityDistribution) {
        this.log = log;
        this.configuration = configuration;
        this.shouldComputeSimilarityDistribution = shouldComputeSimilarityDistribution;
    }

    @Override
    public Pair<RelationshipsWritten, Map<String, Object>> execute(
        Graph graph,
        GraphStore graphStore,
        KnnResult result
    ) {
        var similarityGraphResult = SimilarityResultCompanion.computeToGraph(
            graph,
            graph.nodeCount(),
            configuration.concurrency(),
            result.streamSimilarityResult()
        );

        var similaritySingleTypeRelationshipsHandler = new SimilaritySingleTypeRelationshipsHandler(
            graph,
            () -> similarityGraphResult,
            shouldComputeSimilarityDistribution
        );

        var addRelationshipResult = new MutateRelationshipService(log).mutate(
            graphStore,
            configuration.mutateRelationshipType(),
            configuration.mutateProperty(),
            similaritySingleTypeRelationshipsHandler
        );

        var relationshipsWritten = new RelationshipsWritten(addRelationshipResult.relationshipsAdded());
        var similaritySummary = similaritySingleTypeRelationshipsHandler.similaritySummary();

        return Pair.of(relationshipsWritten, similaritySummary);
    }
}
