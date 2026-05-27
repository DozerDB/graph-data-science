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
package org.neo4j.gds.compression.api;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class CompressionKindTest {

    @Test
    void equalsByName() {
        var a = new CompressionKind("MY_CODEC");
        var b = new CompressionKind("MY_CODEC");
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void notEqualForDifferentNames() {
        assertThat(new CompressionKind("A")).isNotEqualTo(new CompressionKind("B"));
    }

    @Test
    void wellKnownConstantsHaveExpectedNames() {
        assertThat(CompressionKind.DELTA_VAR_LONG.name()).isEqualTo("DELTA_VAR_LONG");
        assertThat(CompressionKind.PACKED.name()).isEqualTo("PACKED");
        assertThat(CompressionKind.UNCOMPRESSED.name()).isEqualTo("UNCOMPRESSED");
        assertThat(CompressionKind.MIXED.name()).isEqualTo("MIXED");
    }

    @Test
    void customKindCanBeDeclaredExternally() {
        var enterpriseKind = new CompressionKind("NATIVE_SVB");
        assertThat(enterpriseKind.name()).isEqualTo("NATIVE_SVB");
    }
}
