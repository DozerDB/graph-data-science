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
package org.neo4j.gds.core.write.resultstore;

import org.junit.jupiter.api.Test;
import org.neo4j.gds.api.CompositeRelationshipIterator;
import org.neo4j.gds.api.EphemeralResultStore;
import org.neo4j.gds.api.GraphStore;

import java.util.List;
import java.util.function.LongUnaryOperator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ResultStoreRelationshipPropertiesExporterTest {

    @Test
    void shouldWriteRelationshipIteratorsToResultStore() {
        var resultStore = new EphemeralResultStore();
        var graphStore = mock(GraphStore.class);
        var relationshipIterator = mock(CompositeRelationshipIterator.class);
        when(graphStore.getCompositeRelationshipIterator(any(), any())).thenReturn(relationshipIterator);
        var toOriginalId = mock(LongUnaryOperator.class);
        new ResultStoreRelationshipPropertiesExporter(graphStore, resultStore, toOriginalId)
            .write("TYPE", List.of("foo", "bar"));

        var relationshipIteratorEntry = resultStore.getRelationshipIterator("TYPE", List.of("foo", "bar"));
        assertThat(relationshipIteratorEntry.relationshipIterator()).isEqualTo(relationshipIterator);
        assertThat(relationshipIteratorEntry.toOriginalId()).isEqualTo(toOriginalId);
    }
}
