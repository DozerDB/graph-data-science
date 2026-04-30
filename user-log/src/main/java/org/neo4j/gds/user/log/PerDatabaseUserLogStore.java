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
package org.neo4j.gds.user.log;

import org.neo4j.gds.api.User;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class PerDatabaseUserLogStore implements UserLogStore {
    private final ConcurrentHashMap<User, PerUserLogStore> logStores = new ConcurrentHashMap<>();

    @Override
    public void addUserLogMessage(User user, GroupingKey groupingKey, String message) {
        var logStore = getUserLogStore(user);

        logStore.addLogMessage(groupingKey, message);
    }

    @Override
    public Stream<UserLogEntry> query(User user) {
        var logStore = getUserLogStore(user);

        return logStore.stream().flatMap(PerDatabaseUserLogStore::terskWithMessagesToUserLogEntryStream);
    }

    private PerUserLogStore getUserLogStore(User user) {
        return logStores.computeIfAbsent(user, __ -> new PerUserLogStore());
    }

    /**
     * One tersk with messages turns into several user log entries
     */
    private static Stream<UserLogEntry> terskWithMessagesToUserLogEntryStream(Map.Entry<GroupingKey, Queue<String>> terskWithMessages) {
        return terskWithMessages.getValue().stream().map(message ->
            {
                var tersk = terskWithMessages.getKey();

                return UserLogEntry.create(
                    tersk.description(),
                    message,
                    tersk.startTime()
                );
            }
        );
    }
}
