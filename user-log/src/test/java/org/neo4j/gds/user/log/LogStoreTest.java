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

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

class LogStoreTest {
    @Test
    void shouldDealWithNewTasks() {
        var logStore = new PerUserLogStore();

        assertThat(logStore.stream()).isEmpty();
    }

    @Test
    void shouldStoreAndRetrieveMessage() {
        var logStore = new PerUserLogStore();

        logStore.addLogMessage(new TestGroupingKey("foo"), "bar");
        var stream = logStore.stream();

        assertThat(stream.map(mapToSomethingUseful())).containsExactly(
            Pair.of("foo", List.of("bar"))
        );
    }

    @Test
    void shouldStoreAndRetrieveMessages() {
        var logStore = new PerUserLogStore();

        logStore.addLogMessage(new TestGroupingKey("foo"), "bar 1");
        logStore.addLogMessage(new TestGroupingKey("foo"), "bar 2");
        logStore.addLogMessage(new TestGroupingKey("baz"), "quux 1");
        var stream = logStore.stream();

        /*
         * Because start time identical, tie-break on description, so order reversed from insertion order :shrug:
         * At least it is stable...
         */
        assertThat(stream.map(mapToSomethingUseful())).containsExactly(
            Pair.of("baz", List.of("quux 1")),
            Pair.of("foo", List.of("bar 1", "bar 2"))
        );
    }

    private static Function<Map.Entry<GroupingKey, Queue<String>>, Pair<String, List<String>>> mapToSomethingUseful() {
        return e -> Pair.of(
            e.getKey().description(),
            (List<String>) new ArrayList<>(e.getValue())
        );
    }

    @Test
    void shouldCapNumberOfTersksTracked() {
        var logStore = new PerUserLogStore(3);

        logStore.addLogMessage(new TestGroupingKey("task description 1"), "log message 11");
        logStore.addLogMessage(new TestGroupingKey("task description 1"), "log message 12");
        logStore.addLogMessage(new TestGroupingKey("task description 1"), "log message 13");
        logStore.addLogMessage(new TestGroupingKey("task description 2"), "log message 21");
        logStore.addLogMessage(new TestGroupingKey("task description 3"), "log message 31");
        logStore.addLogMessage(new TestGroupingKey("task description 3"), "log message 32");

        assertThat(logStore.stream().map(mapToSomethingUseful())).containsExactly(
            Pair.of("task description 1", List.of("log message 11", "log message 12", "log message 13")),
            Pair.of("task description 2", List.of("log message 21")),
            Pair.of("task description 3", List.of("log message 31", "log message 32"))
        );

        logStore.addLogMessage(new TestGroupingKey("task description 4"), "log message 41");

        assertThat(logStore.stream().map(mapToSomethingUseful())).containsExactly(
            Pair.of("task description 2", List.of("log message 21")),
            Pair.of("task description 3", List.of("log message 31", "log message 32")),
            Pair.of("task description 4", List.of("log message 41"))
        );
    }
}
