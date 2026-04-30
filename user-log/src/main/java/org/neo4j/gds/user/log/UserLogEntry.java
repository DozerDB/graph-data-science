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

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;

public final class UserLogEntry {
    public String taskName;
    public String message;
    public LocalTime timeStarted;

    private UserLogEntry(String taskName, String message, LocalTime timeStarted) {
        this.taskName = taskName;
        this.message = message;
        this.timeStarted = timeStarted;
    }

    public static UserLogEntry create(String description, String message, long startTime) {
        var instant = Instant.ofEpochMilli(startTime);
        var localTime = LocalTime.ofInstant(instant, ZoneId.systemDefault());
        return new UserLogEntry(description, message, localTime);
    }

    public String getTaskName() {
        return taskName;
    }

    public String getMessage() {
        return message;
    }

    public LocalTime getTimeStarted() {
        return timeStarted;
    }
}
