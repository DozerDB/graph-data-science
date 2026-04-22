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
package org.neo4j.gds.api;

import java.util.Objects;
import java.util.Optional;

// This would be a record, except JOL doesn't like records and we need JOL to like our GraphStore implementations
public final class DatabaseInfo {
    private final DatabaseId databaseId;
    private final DatabaseLocation databaseLocation;
    private final Optional<DatabaseId> remoteDatabaseId;

    public enum DatabaseLocation {
        LOCAL, REMOTE, NONE
    }

    public static DatabaseInfo create(DatabaseId databaseId, DatabaseLocation databaseLocation) {
        return create(databaseId, databaseLocation, Optional.empty());
    }

    public static DatabaseInfo create(DatabaseId databaseId, DatabaseLocation databaseLocation, DatabaseId remoteDatabaseId) {
        return create(databaseId, databaseLocation, Optional.of(remoteDatabaseId));
    }

    public static DatabaseInfo create(DatabaseId databaseId, DatabaseLocation databaseLocation, Optional<DatabaseId> remoteDatabaseId) {
        validate(databaseLocation, remoteDatabaseId);
        return new DatabaseInfo(databaseId, databaseLocation, remoteDatabaseId);
    }

    private static void validate(DatabaseLocation databaseLocation, Optional<DatabaseId> remoteDatabaseId) {
        if (databaseLocation == DatabaseLocation.REMOTE) {
            if (remoteDatabaseId.isEmpty()) {
                throw new IllegalStateException("Remote database id must be set when database location is remote");
            }
        } else {
            if (remoteDatabaseId.isPresent()) {
                throw new IllegalStateException("Remote database id must not be set when database location is not remote");
            }
        }
    }

    private DatabaseInfo(DatabaseId databaseId, DatabaseLocation databaseLocation, Optional<DatabaseId> remoteDatabaseId) {
        this.databaseId = databaseId;
        this.databaseLocation = databaseLocation;
        this.remoteDatabaseId = remoteDatabaseId;
    }

    public DatabaseId databaseId() {
        return databaseId;
    }

    public DatabaseLocation databaseLocation() {
        return databaseLocation;
    }

    public Optional<DatabaseId> remoteDatabaseId() {
        return remoteDatabaseId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        return other instanceof DatabaseInfo otherDatabaseInfo
            && databaseId.equals(otherDatabaseInfo.databaseId)
            && databaseLocation.equals(otherDatabaseInfo.databaseLocation)
            && Objects.equals(remoteDatabaseId, otherDatabaseInfo.remoteDatabaseId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(databaseId, databaseLocation, remoteDatabaseId);
    }
}
