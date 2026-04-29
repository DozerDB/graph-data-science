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

import org.neo4j.values.storable.Float32Vector;
import org.neo4j.values.storable.Float64Vector;
import org.neo4j.values.storable.Int16Vector;
import org.neo4j.values.storable.Int32Vector;
import org.neo4j.values.storable.Int64Vector;
import org.neo4j.values.storable.Int8Vector;
import org.neo4j.values.storable.ValueWriter;

public final class GdsNeo4jVectorValuesCollector {

    private GdsNeo4jVectorValuesCollector() {}

    private static final class Ref<T> {
        T value;
    }

    public static double[] collectFloat64(Float64Vector value) {
        Ref<double[]> ref = new Ref<>();
        value.writeTo(new ValueWriter.Adapter<>() {
            @Override
            public void writeFloat64Vector(double[] values) {
                ref.value = values;
            }
        });
        return ref.value;
    }

    public static float[] collectFloat32(Float32Vector value) {
        Ref<float[]> ref = new Ref<>();
        value.writeTo(new ValueWriter.Adapter<>() {
            @Override
            public void writeFloat32Vector(float[] values) {
                ref.value = values;
            }
        });
        return ref.value;
    }

    public static long[] collectInt64(Int64Vector value) {
        Ref<long[]> ref = new Ref<>();
        value.writeTo(new ValueWriter.Adapter<>() {
            @Override
            public void writeInt64Vector(long[] values) {
                ref.value = values;
            }
        });
        return ref.value;
    }

    public static int[] collectInt32(Int32Vector value) {
        Ref<int[]> ref = new Ref<>();
        value.writeTo(new ValueWriter.Adapter<>() {
            @Override
            public void writeInt32Vector(int[] values) {
                ref.value = values;
            }
        });
        return ref.value;
    }

    public static short[] collectInt16(Int16Vector value) {
        Ref<short[]> ref = new Ref<>();
        value.writeTo(new ValueWriter.Adapter<>() {
            @Override
            public void writeInt16Vector(short[] values) {
                ref.value = values;
            }
        });
        return ref.value;
    }

    public static byte[] collectInt8(Int8Vector value) {
        Ref<byte[]> ref = new Ref<>();
        value.writeTo(new ValueWriter.Adapter<>() {
            @Override
            public void writeInt8Vector(byte[] values) {
                ref.value = values;
            }
        });
        return ref.value;
    }

}
