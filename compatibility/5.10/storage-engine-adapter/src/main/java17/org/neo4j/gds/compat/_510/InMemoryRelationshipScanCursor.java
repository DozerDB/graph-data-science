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
package org.neo4j.gds.compat._510;

import org.neo4j.gds.core.cypher.CypherGraphStore;
import org.neo4j.internal.recordstorage.AbstractInMemoryRelationshipScanCursor;
import org.neo4j.storageengine.api.AllRelationshipsScan;
import org.neo4j.storageengine.api.LongReference;
import org.neo4j.storageengine.api.PropertySelection;
import org.neo4j.storageengine.api.Reference;
import org.neo4j.storageengine.api.StoragePropertyCursor;
import org.neo4j.token.TokenHolders;

public class InMemoryRelationshipScanCursor extends AbstractInMemoryRelationshipScanCursor {

    public InMemoryRelationshipScanCursor(
        CypherGraphStore graphStore,
        TokenHolders tokenHolders
    ) {
        super(graphStore, tokenHolders);
    }

    @Override
    public void single(long reference, long sourceNodeReference, int type, long targetNodeReference) {
        single(reference);
    }

    @Override
    public Reference propertiesReference() {
        return LongReference.longReference(getId());
    }

    @Override
    public void properties(
        StoragePropertyCursor storagePropertyCursor, PropertySelection propertySelection
    ) {
        properties(storagePropertyCursor, new InMemoryPropertySelectionImpl(propertySelection));
    }

    @Override
    public boolean scanBatch(AllRelationshipsScan allRelationshipsScan, long sizeHint) {
        return super.scanBatch(allRelationshipsScan, (int) sizeHint);
    }
}
