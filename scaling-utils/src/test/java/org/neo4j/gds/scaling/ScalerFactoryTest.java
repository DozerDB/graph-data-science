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
package org.neo4j.gds.scaling;

import org.junit.jupiter.api.Test;
import org.neo4j.gds.scaling.scale.ScalerType;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScalerFactoryTest {

    @Test
    void parse() {
        assertThat(ScalerParser.parse("log").type() == ScalerType.Log);
        assertThat(ScalerParser.parse("minmax").type() == ScalerType.MinMax);
        assertThat(ScalerParser.parse("max").type() == ScalerType.Max);
        assertThat(ScalerParser.parse("center").type() == ScalerType.Center);
        assertThat(ScalerParser.parse("l1norm").type() == ScalerType.L1Norm);
        assertThat(ScalerParser.parse("l2norm").type() == ScalerType.L2Norm);
        assertThat(ScalerParser.parse("mean").type() == ScalerType.Mean);
        assertThat(ScalerParser.parse("stdscore").type() == ScalerType.Std);
        assertThat(ScalerParser.parse("none").type() == ScalerType.None);

        // case insensitive
        assertThat(ScalerParser.parse("L1NORM").type() == ScalerType.L1Norm);
        assertThat(ScalerParser.parse("StdScore").type() == ScalerType.Std);

        // nested syntax
        assertThat(ScalerParser.parse(Map.of("type", "log")).type() == ScalerType.Log);
        assertThat(ScalerParser.parse(Map.of("type", "log", "offset", 10)).type() == ScalerType.Log);
        assertThat(ScalerParser.parse(Map.of("type", "minmax")).type() == ScalerType.MinMax);
        assertThat(ScalerParser.parse(Map.of("type", "STDSCORE")).type() == ScalerType.Std);
        assertThat(ScalerParser.parse(Map.of("type", "CEntEr")).type() == ScalerType.Center);
    }

    @Test
    void badInput() {
        // bad strings
        assertThatThrownBy(() -> ScalerParser.parse("mean  ")).hasMessageContaining("Unrecognised scaler type specified: `mean  `.");
        assertThatThrownBy(() -> ScalerParser.parse("yo")).hasMessageContaining("Unrecognised scaler type specified: `yo`.");

        // bad types
        assertThatThrownBy(() -> ScalerParser.parse(1L)).hasMessageContaining("Unrecognised scaler type specified: `1`.");
        assertThatThrownBy(() -> ScalerParser.parse(42D)).hasMessageContaining("Unrecognised scaler type specified: `42.0`.");
        assertThatThrownBy(() -> ScalerParser.parse(List.of("mean"))).hasMessageContaining("Unrecognised scaler type specified: `[mean]`.");
        assertThatThrownBy(() -> ScalerParser.parse(Map.of("mean", "scaler"))).hasMessageContaining("Unrecognised scaler type specified: `{mean=scaler}`.");
        assertThatThrownBy(() -> ScalerParser.parse(false)).hasMessageContaining("Unrecognised scaler type specified: `false`.");

        // bad nested syntax
        assertThatThrownBy(() -> ScalerParser.parse(Map.of("type", "lag"))).hasMessageContaining("Unrecognised scaler type specified: `lag`.");
        assertThatThrownBy(() -> ScalerParser.parse(Map.of("type", "log", "offset", false))).hasMessageContaining("The value of `offset` must be of type `Number` but was `Boolean`.");
        assertThatThrownBy(() -> ScalerParser.parse(Map.of("type", "log", "offsat", 0.0))).hasMessageContaining("Unexpected configuration key: offsat");
    }

    @Test
    void shouldAcceptWorkingTypes(){
        assertThat(ScalerParser.parse("log").workingScaler()).isTrue();
        assertThat(ScalerParser.parse("none").workingScaler()).isFalse();
    }
}
