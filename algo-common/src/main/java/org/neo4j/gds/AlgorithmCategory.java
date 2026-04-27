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

import java.util.Arrays;
import java.util.List;

import static org.neo4j.gds.utils.StringFormatting.formatWithLocale;
import static org.neo4j.gds.utils.StringFormatting.toLowerCaseWithLocale;
import static org.neo4j.gds.utils.StringFormatting.toUpperCaseWithLocale;

public enum AlgorithmCategory {
    COMMUNITY_DETECTION("community-detection"),
    CENTRALITY("centrality"),
    SIMILARITY("similarity"),
    PATH_FINDING("path-finding"),
    NODE_EMBEDDING("node-embedding"),
    MACHINE_LEARNING("machine-learning");

    private final String slug;

    AlgorithmCategory(String slug) {
        this.slug = slug;
    }

    @Override
    public String toString() {
        return slug;
    }

    private static final List<String> VALUES = Arrays
        .stream(AlgorithmCategory.values())
        .map(AlgorithmCategory::toString)
        .toList();

    public static AlgorithmCategory parse(Object input) {
        if (input instanceof String) {
            var inputString = (String) input;
            var normalized_input = toLowerCaseWithLocale(inputString).replace("_", "-");

            if (VALUES.contains(normalized_input)) {
                var category_enum_value = toUpperCaseWithLocale(inputString.replace("-", "_"));
                return AlgorithmCategory.valueOf(category_enum_value);
            }

            throw new IllegalArgumentException(formatWithLocale(
                "AlgorithmCategory `%s` is not supported. Must be one of: [%s].",
                input,
                String.join(",", VALUES)
            ));
        } else if (input instanceof AlgorithmCategory) {
            return (AlgorithmCategory) input;
        }

        throw new IllegalArgumentException(formatWithLocale(
            "Expected AlgorithmCategory or String. Got `%s` of type `%s`.",
            input,
            input.getClass().getSimpleName()
        ));
    }
}
