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
package org.neo4j.gds.ml.models.linearregression;

import org.neo4j.gds.ml.api.TrainingMethod;
import org.neo4j.gds.ml.core.functions.Weights;
import org.neo4j.gds.ml.core.tensor.Matrix;
import org.neo4j.gds.ml.core.tensor.Scalar;
import org.neo4j.gds.ml.models.Regressor;

public record LinearRegressionData(Weights<Matrix> weights, Weights<Scalar> bias) implements Regressor.RegressorData {

    @Override
    public TrainingMethod trainerMethod() {
        return TrainingMethod.LinearRegression;
    }

    @Override
    public int featureDimension() {
        return weights().data().cols();
    }

    public static LinearRegressionData of(int featureDimension) {
        return new LinearRegressionData(
            Weights.ofMatrix(1, featureDimension),
            Weights.ofScalar(0D)
        );
    }
}
