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
package org.neo4j.gds.values;

import org.jetbrains.annotations.NotNull;
import org.neo4j.gds.values.primitive.PrimitiveValues;
import org.neo4j.values.AnyValue;
import org.neo4j.values.SequenceValue;
import org.neo4j.values.storable.ArrayValue;
import org.neo4j.values.storable.IntegralValue;
import org.neo4j.values.storable.NoValue;
import org.neo4j.values.storable.Value;
import org.neo4j.values.storable.ValueGroup;
import org.neo4j.values.storable.ValueWriter;
import org.neo4j.values.virtual.ListValue;

import static org.neo4j.gds.utils.StringFormatting.formatWithLocale;

public final class GdsNeo4jValueConverter {

    public static GdsValue toValue(@NotNull AnyValue value) {
        if (value == NoValue.NO_VALUE) {
            return GdsNoValue.NO_VALUE;
        }
        if (value.isSequenceValue()) { // ArrayValue or ListValue
            return convertSequenceValueOrFail((SequenceValue) value);
        }
        if (value instanceof Value storableValue && storableValue.valueGroup() == ValueGroup.NUMBER) {
            if (storableValue instanceof org.neo4j.values.storable.FloatValue floatValue) {
                return PrimitiveValues.floatingPointValue(floatValue.floatValue());
            } else if (storableValue instanceof org.neo4j.values.storable.DoubleValue doubleValue) {
                return PrimitiveValues.floatingPointValue(doubleValue.doubleValue());
            } else if (storableValue instanceof IntegralValue integralValue) {
                return PrimitiveValues.longValue(integralValue.longValue());
            }
        }
        if (value instanceof org.neo4j.values.storable.FloatingPointVector floatingPointVector) {
            switch (floatingPointVector) {
                case org.neo4j.values.storable.Float32Vector float32Vector -> {
                    float[] collector = new float[float32Vector.dimensions()];
                    for (int i = 0; i < float32Vector.dimensions(); i++) {
                        collector[i] = float32Vector.floatValue(i);
                    }
                    return PrimitiveValues.floatArray(collector);
                }
                case org.neo4j.values.storable.Float64Vector float64Vector -> {
                    double[] collector = new double[float64Vector.dimensions()];
                    for (int i = 0; i < float64Vector.dimensions(); i++) {
                        collector[i] = float64Vector.doubleValue(i);
                    }
                    return PrimitiveValues.doubleArray(collector);
                }
            }
        }
        if (value instanceof org.neo4j.values.storable.IntegralVector integralVector) {
            switch (integralVector) {
                case org.neo4j.values.storable.Int64Vector int64Vector -> {
                    final long[][] collector = new long[1][];
                    int64Vector.writeTo(new ValueWriter.Adapter<>() {
                        @Override
                        public void writeInt64Vector(long[] values) {
                            collector[0] = values;
                        }
                    });
                    return PrimitiveValues.longArray(collector[0]);
                }
                case org.neo4j.values.storable.Int32Vector int32Vector -> {
                    int[] collector = new int[int32Vector.dimensions()];
                    for (int i = 0; i < int32Vector.dimensions(); i++) {
                        collector[i] = (int) int32Vector.doubleValue(i);
                    }
                    return PrimitiveValues.intArray(collector);
                }
                case org.neo4j.values.storable.Int16Vector int16Vector -> {
                    short[] collector = new short[int16Vector.dimensions()];
                    for (int i = 0; i < int16Vector.dimensions(); i++) {
                        collector[i] = (short) int16Vector.doubleValue(i);
                    }
                    return PrimitiveValues.shortArray(collector);
                }
                case org.neo4j.values.storable.Int8Vector int8Vector -> {
                    byte[] collector = new byte[int8Vector.dimensions()];
                    for (int i = 0; i < int8Vector.dimensions(); i++) {
                        collector[i] = (byte) int8Vector.doubleValue(i);
                    }
                    return PrimitiveValues.byteArray(collector);
                }
            }
        }
        throw new IllegalArgumentException(formatWithLocale(
            "Unsupported conversion to GDS Value from Neo4j Value with type `%s`.",
            value.getTypeName()
        ));
    }

    private static GdsValue convertSequenceValueOrFail(SequenceValue value) {
        if (value instanceof ListValue listValue) {
            return convertListValueOrFail(listValue);
        } else if (value instanceof ArrayValue arrayValue) {
            return convertArrayValueOrFail(arrayValue);
        } else {
            throw failOnBadInput(value);
        }
    }

    @NotNull
    private static Array convertListValueOrFail(ListValue listValue) {
        if (listValue.isEmpty()) {
            // encode as long array
            return PrimitiveValues.EMPTY_LONG_ARRAY;
        }
        try {
            return convertArrayValueOrFail(listValue.toStorableArray());
        } catch (RuntimeException e) {
            throw failOnBadInput(listValue);
        }
    }

    @NotNull
    private static Array convertArrayValueOrFail(ArrayValue array) {
        if (array.valueGroup() != ValueGroup.NUMBER_ARRAY) {
            throw failOnBadInput(array);
        }
        if (array.isEmpty()) {
            return PrimitiveValues.EMPTY_LONG_ARRAY;
        }
        var arrayCopy = array.asObjectCopy();
        if (arrayCopy instanceof byte[]) {
            return PrimitiveValues.byteArray((byte[]) arrayCopy);
        } else if (arrayCopy instanceof short[]) {
            return PrimitiveValues.shortArray((short[]) arrayCopy);
        } else if (arrayCopy instanceof int[]) {
            return PrimitiveValues.intArray((int[]) arrayCopy);
        } else if (arrayCopy instanceof long[]) {
            return PrimitiveValues.longArray((long[]) arrayCopy);
        } else if (arrayCopy instanceof double[]) {
            return PrimitiveValues.doubleArray((double[]) arrayCopy);
        } else if (arrayCopy instanceof float[]) {
            return PrimitiveValues.floatArray((float[]) arrayCopy);
        } else {
            throw failOnBadInput(array);
        }
    }

    private static IllegalArgumentException failOnBadInput(SequenceValue badInput) {
        return new IllegalArgumentException(
            formatWithLocale(
                "Unsupported conversion to GDS Value from Neo4j Value `%s`.",
                badInput
            )
        );
    }

    private GdsNeo4jValueConverter() {}
}
