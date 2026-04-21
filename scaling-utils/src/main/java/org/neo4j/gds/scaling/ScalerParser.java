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

import org.jetbrains.annotations.NotNull;
import org.neo4j.gds.core.CypherMapWrapper;
import org.neo4j.gds.scaling.scale.ScalerType;
import org.neo4j.gds.utils.StringJoining;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.neo4j.gds.utils.StringFormatting.formatWithLocale;
import static org.neo4j.gds.utils.StringFormatting.toLowerCaseWithLocale;

public final class ScalerParser {

    private ScalerParser() {}

    private static final String SCALER_KEY = "type";
    public static final Map<String, ScalerType> SUPPORTED_SCALERS = Stream.of(
        ScalerType.None,
        ScalerType.Mean,
        ScalerType.Max,
        ScalerType.Log,
        ScalerType.Center,
        ScalerType.Std,
        ScalerType.L1Norm,
        ScalerType.L2Norm,
        ScalerType.MinMax
    ).collect(Collectors.toMap(ScalerType::scalerName, scalerType -> scalerType));

    private static final String OFFSET_KEY = "offset";
    private static final Map<ScalerType, List<String>> REQUIRED_SCALER_KEYS = Map.of(
        ScalerType.Log, List.of(OFFSET_KEY)
    );

    public static ScalerType parseName(String spec) {
        return Optional.ofNullable(SUPPORTED_SCALERS.get(toLowerCaseWithLocale(spec)))
            .orElseThrow(invalidScalerExceptionSupplier(spec));
    }

    public static ScalerFactory parse(Object userInput) {
        if (userInput instanceof ScalerFactory) {
            return (ScalerFactory) userInput;
        }
        if (userInput instanceof String specName) {
            return parse(Map.of(SCALER_KEY, specName));
        }
        if (userInput instanceof Map) {
            var inputMap = (Map<String, Object>) userInput;
            var scalerSpec = inputMap.get(SCALER_KEY);
            if (scalerSpec instanceof String specName) {
                var scalerType = parseName(specName);
                var mapWrapper = CypherMapWrapper.create(inputMap).withoutEntry(SCALER_KEY);
                mapWrapper.requireOnlyKeysFrom(REQUIRED_SCALER_KEYS.getOrDefault(scalerType, List.of()));
                double offset = mapWrapper.getNumber(OFFSET_KEY, 0.0).doubleValue();
                return ScalerFactory.of(scalerType, specName, offset);
            }
        }
        return throwForInvalidScaler(userInput);
    }

    private static @NotNull Supplier<IllegalArgumentException> invalidScalerExceptionSupplier(Object inputScaler) {
        return () -> new IllegalArgumentException(formatWithLocale(
            "Unrecognised scaler type specified: `%s`. Expected one of: %s.",
            inputScaler,
            StringJoining.join(SUPPORTED_SCALERS.keySet())
        ));
    }

    private static ScalerFactory throwForInvalidScaler(Object inputScaler) {
        throw invalidScalerExceptionSupplier(inputScaler).get();
    }
}
