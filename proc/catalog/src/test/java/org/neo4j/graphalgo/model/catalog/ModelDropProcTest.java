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
package org.neo4j.graphalgo.model.catalog;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.graphalgo.core.model.Model;
import org.neo4j.graphalgo.core.model.ModelCatalog;

import java.time.ZonedDateTime;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.hamcrest.core.Is.isA;
import static org.neo4j.graphalgo.compat.MapUtil.map;
import static org.neo4j.graphalgo.utils.StringFormatting.formatWithLocale;

class ModelDropProcTest extends ModelProcBaseTest {

    @BeforeEach
    void setUp() throws Exception {
        registerProcedures(ModelDropProc.class);
    }

    @Test
    void dropsModel() {
        String existingModel = "testModel";
        String testModelType = "testAlgo";

        TestTrainConfig trainConfig = TestTrainConfig.of();
        ModelCatalog.set(
            Model.of(
                getUsername(),
                existingModel,
                testModelType,
                GRAPH_SCHEMA,
                "testData",
                trainConfig
            )
        );

        var dropQuery = "CALL gds.beta.model.drop($modelName)";
        assertCypherResult(
            dropQuery,
            Map.of("modelName", existingModel),
            singletonList(
                map(
                    "modelInfo", map("modelName", existingModel, "modelType", testModelType),
                    "trainConfig", map(
                        "dummyConfigProperty", trainConfig.dummyConfigProperty(),
                        "modelName", trainConfig.modelName(),
                        "sudo", trainConfig.sudo()
                    ),
                    "graphSchema", EXPECTED_SCHEMA,
                    "creationTime", isA(ZonedDateTime.class)
                )
            )
        );
    }

    @Test
    void failOnDroppingNonExistingModel() {
        String modelName = "foo";
        assertError(
            "CALL gds.beta.model.drop($modelName)",
            map("modelName", modelName),
            formatWithLocale("Model with name `%s` does not exist and can't be removed.", modelName)
        );
    }
}
