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
package org.neo4j.gds.api.schema;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.neo4j.gds.NodeLabel;
import org.neo4j.gds.api.DefaultValue;
import org.neo4j.gds.api.PropertyState;
import org.neo4j.gds.api.nodeproperties.ValueType;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


public class NodeSchemaConversionTest {

    @Nested
    class ToRecordTypeTests {
        @Test
        void convertEmptySchema() {
            var nodeSchema = MutableNodeSchema.empty();

            var result = NodeSchemaUtils.toRecordType(nodeSchema);

            assertThat(result).isEqualTo(NodeSchemaRecord.empty());
        }

        @Test
        void convertSimpleSchema() {
            var nodeSchema = MutableNodeSchema.empty()
                .addLabel(NodeLabel.of("FOO"), Map.of(
                    "p0", PropertySchema.of("p0", ValueType.DOUBLE, DefaultValue.of(4D), PropertyState.PERSISTENT),
                    "p1", PropertySchema.of("p1", ValueType.LONG, DefaultValue.of(42L), PropertyState.TRANSIENT)
                ))
                .addLabel(NodeLabel.of("BAR"));

            var result = NodeSchemaUtils.toRecordType(nodeSchema);

            var expected = NodeSchemaRecord.builder()
                .addProperty("FOO", "p0", ValueType.DOUBLE, DefaultValue.of(4D), PropertyState.PERSISTENT)
                .addProperty("FOO", "p1", ValueType.LONG, DefaultValue.of(42L), PropertyState.TRANSIENT)
                .addLabel("BAR")
                .build();
            assertThat(result).isEqualTo(expected);
        }

        @Test
        void convertCompleteSchema() {
            var nodeSchema = MutableNodeSchema.empty()
                .addLabel(NodeLabel.ALL_NODES, Map.of(
                    "p0", PropertySchema.of("p0", ValueType.LONG, DefaultValue.of(42L), PropertyState.TRANSIENT),
                    "p1", PropertySchema.of("p1", ValueType.DOUBLE, DefaultValue.of(4D), PropertyState.PERSISTENT)
                ))
                .addLabel(NodeLabel.of("FOO"), Map.of(
                    "p2", PropertySchema.of("p2", ValueType.DOUBLE),
                    "p3", PropertySchema.of("p3", ValueType.LONG_ARRAY),
                    "p4", PropertySchema.of("p4", ValueType.DOUBLE_ARRAY),
                    "p5", PropertySchema.of("p5", ValueType.FLOAT_ARRAY)
                ))
                .addLabel(NodeLabel.of("BAR"), Map.of(
                    "p4", PropertySchema.of("p4", ValueType.DOUBLE_ARRAY),
                    "p5", PropertySchema.of("p5", ValueType.FLOAT_ARRAY)
                ))
                .addLabel(NodeLabel.of("BAZ"));

            var result = NodeSchemaUtils.toRecordType(nodeSchema);

            var expected = NodeSchemaRecord.builder()
                .addProperty(NodeLabel.ALL_LABEL, "p0", ValueType.LONG, DefaultValue.of(42L), PropertyState.TRANSIENT)
                .addProperty(NodeLabel.ALL_LABEL, "p1", ValueType.DOUBLE, DefaultValue.of(4D), PropertyState.PERSISTENT)
                .addProperty("FOO", "p2", ValueType.DOUBLE)
                .addProperty("FOO", "p3", ValueType.LONG_ARRAY)
                .addProperty("FOO", "p4", ValueType.DOUBLE_ARRAY)
                .addProperty("FOO", "p5", ValueType.FLOAT_ARRAY)
                .addProperty("BAR", "p4", ValueType.DOUBLE_ARRAY)
                .addProperty("BAR", "p5", ValueType.FLOAT_ARRAY)
                .addLabel("BAZ")
                .build();
            assertThat(result).isEqualTo(expected);
        }

    }

    @Nested
    class FromRecordTypeTests {

        @Test
        void convertEmptySchema() {
            var nodeSchema = NodeSchemaRecord.empty();

            var result = NodeSchemaUtils.fromRecordType(nodeSchema);

            assertThat(result).isEqualTo(MutableNodeSchema.empty());
        }

        @Test
        void convertSimpleSchema() {
            var nodeSchema = NodeSchemaRecord.builder()
                .addProperty("FOO", "p0", ValueType.DOUBLE, DefaultValue.of(4D), PropertyState.PERSISTENT)
                .addProperty("FOO", "p1", ValueType.LONG, DefaultValue.of(42L), PropertyState.TRANSIENT)
                .addLabel("BAR")
                .build();

            var result = NodeSchemaUtils.fromRecordType(nodeSchema);

            var expected = MutableNodeSchema.empty()
                .addLabel(NodeLabel.of("FOO"), Map.of(
                    "p0", PropertySchema.of("p0", ValueType.DOUBLE, DefaultValue.of(4D), PropertyState.PERSISTENT),
                    "p1", PropertySchema.of("p1", ValueType.LONG, DefaultValue.of(42L), PropertyState.TRANSIENT)
                ))
                .addLabel(NodeLabel.of("BAR"));
            assertThat(result).isEqualTo(expected);
        }

        @Test
        void convertCompleteSchema() {
            var nodeSchema = NodeSchemaRecord.builder()
                .addProperty(NodeLabel.ALL_LABEL, "p0", ValueType.LONG, DefaultValue.of(42L), PropertyState.TRANSIENT)
                .addProperty(NodeLabel.ALL_LABEL, "p1", ValueType.DOUBLE, DefaultValue.of(4D), PropertyState.PERSISTENT)
                .addProperty("FOO", "p2", ValueType.DOUBLE)
                .addProperty("FOO", "p3", ValueType.LONG_ARRAY)
                .addProperty("FOO", "p4", ValueType.DOUBLE_ARRAY)
                .addProperty("FOO", "p5", ValueType.FLOAT_ARRAY)
                .addProperty("BAR", "p4", ValueType.DOUBLE_ARRAY)
                .addProperty("BAR", "p5", ValueType.FLOAT_ARRAY)
                .addLabel("BAZ")
                .build();

            var result = NodeSchemaUtils.fromRecordType(nodeSchema);

            var expected = MutableNodeSchema.empty()
                .addLabel(NodeLabel.ALL_NODES, Map.of(
                    "p0", PropertySchema.of("p0", ValueType.LONG, DefaultValue.of(42L), PropertyState.TRANSIENT),
                    "p1", PropertySchema.of("p1", ValueType.DOUBLE, DefaultValue.of(4D), PropertyState.PERSISTENT)
                ))
                .addLabel(NodeLabel.of("FOO"), Map.of(
                    "p2", PropertySchema.of("p2", ValueType.DOUBLE),
                    "p3", PropertySchema.of("p3", ValueType.LONG_ARRAY),
                    "p4", PropertySchema.of("p4", ValueType.DOUBLE_ARRAY),
                    "p5", PropertySchema.of("p5", ValueType.FLOAT_ARRAY)
                ))
                .addLabel(NodeLabel.of("BAR"), Map.of(
                    "p4", PropertySchema.of("p4", ValueType.DOUBLE_ARRAY),
                    "p5", PropertySchema.of("p5", ValueType.FLOAT_ARRAY)
                ))
                .addLabel(NodeLabel.of("BAZ"));
            assertThat(result).isEqualTo(expected);
        }
    }

    @Test
    void roundTrip() {
        var nodeSchema = MutableNodeSchema.empty()
            .addLabel(NodeLabel.ALL_NODES, Map.of(
                "p0", PropertySchema.of("p0", ValueType.LONG, DefaultValue.of(42L), PropertyState.TRANSIENT),
                "p1", PropertySchema.of("p1", ValueType.DOUBLE, DefaultValue.of(4D), PropertyState.PERSISTENT)
            ))
            .addLabel(NodeLabel.of("FOO"), Map.of(
                "p2", PropertySchema.of("p2", ValueType.DOUBLE),
                "p3", PropertySchema.of("p3", ValueType.LONG_ARRAY),
                "p4", PropertySchema.of("p4", ValueType.DOUBLE_ARRAY),
                "p5", PropertySchema.of("p5", ValueType.FLOAT_ARRAY)
            ))
            .addLabel(NodeLabel.of("BAR"), Map.of(
                "p4", PropertySchema.of("p4", ValueType.DOUBLE_ARRAY),
                "p5", PropertySchema.of("p5", ValueType.FLOAT_ARRAY)
            ))
            .addLabel(NodeLabel.of("BAZ"));

        var result = NodeSchemaUtils.fromRecordType(NodeSchemaUtils.toRecordType(nodeSchema));

        assertThat(result).isEqualTo(nodeSchema);
    }
}
