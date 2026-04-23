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
package org.neo4j.gds;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AlgorithmCategoryTest {

    @ParameterizedTest
    @ValueSource(strings = {"node_embedding", "NODE-EMBEDDing", "node-embedding"})
    void testValidParse(String input) {
        assertThat(AlgorithmCategory.parse(input)).isEqualTo(AlgorithmCategory.NODE_EMBEDDING);
    }

    @Test
    void testInvalidCategoryParse() {
        assertThatThrownBy(() -> AlgorithmCategory.parse("invalid")).hasMessageContaining("AlgorithmCategory `invalid` is not supported. Must be one of: [");
    }

    @Test
    void testInvalidTypeParse() {
        assertThatThrownBy(() -> AlgorithmCategory.parse(1)).hasMessageContaining("Expected AlgorithmCategory or String. Got `1` of type `Integer`.");
    }

}