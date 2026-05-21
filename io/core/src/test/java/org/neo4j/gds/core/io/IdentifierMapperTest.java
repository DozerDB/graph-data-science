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
package org.neo4j.gds.core.io;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class IdentifierMapperTest {

    @Test
    void cachesIdentifierForSameInput() {
        var mapper = IdentifierMapper.<String>builder("foo_");
        assertThat(mapper.getOrCreateIdentifierFor("FOO")).isEqualTo("foo_1");
        assertThat(mapper.getOrCreateIdentifierFor("FOO")).isEqualTo("foo_1");
    }

    @Test
    void incrementsCounterStartingAtOneForDifferentInput() {
        var mapper = IdentifierMapper.<String>builder("foo_");
        assertThat(mapper.getOrCreateIdentifierFor("FOO")).isEqualTo("foo_1");
        assertThat(mapper.getOrCreateIdentifierFor("BAR")).isEqualTo("foo_2");
    }

    @Test
    void canSetAMappingFromOutside() {
        var mapper = IdentifierMapper.<String>builder("foo_");
        mapper.setIdentifierMapping("FOO", "foo_1");
        assertThat(mapper.getOrCreateIdentifierFor("FOO")).isEqualTo("foo_1");

        assertThatCode(() -> mapper.setIdentifierMapping("FOO", "foo_2"))
            .hasMessageContaining("Name=FOO")
            .hasMessageContaining("Identifier1=foo_1")
            .hasMessageContaining("Identifier2=foo_2");
    }

    @Test
    void returnsAllIdentifiers() {
        var mapper = IdentifierMapper.<String>builder("foo_");
        mapper.getOrCreateIdentifierFor("FOO");
        mapper.getOrCreateIdentifierFor("BAR");
        assertThat(mapper.build().identifiers()).containsExactlyInAnyOrder("foo_1", "foo_2");
    }

    @Test
    void iteratesInOrder() {
        var builder = IdentifierMapper.<String>builder("foo_");
        Stream.of("Foo", "Bar", "Baz").forEach(builder::getOrCreateIdentifierFor);
        var mapper = builder.build();

        var result = new ArrayList<String>();
        mapper.forEach((element, identifier) -> result.add(element));

        assertThat(result).isEqualTo(List.of("Bar", "Baz", "Foo"));
    }
}
