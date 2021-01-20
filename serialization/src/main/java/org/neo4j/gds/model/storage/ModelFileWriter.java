/*
 * Copyright (c) 2017-2021 "Neo4j,"
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
package org.neo4j.gds.model.storage;

import com.google.protobuf.GeneratedMessageV3;
import org.neo4j.gds.embeddings.graphsage.GraphSageModelSerializer;
import org.neo4j.gds.embeddings.graphsage.ModelData;
import org.neo4j.gds.embeddings.graphsage.algo.GraphSage;
import org.neo4j.graphalgo.config.BaseConfig;
import org.neo4j.graphalgo.config.ModelConfig;
import org.neo4j.graphalgo.core.model.Model;
import org.neo4j.graphalgo.core.model.ModelMetaDataSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;

import static org.neo4j.gds.model.storage.ModelToFileExporter.META_DATA_SUFFIX;
import static org.neo4j.gds.model.storage.ModelToFileExporter.MODEL_DATA_SUFFIX;
import static org.neo4j.graphalgo.utils.StringFormatting.formatWithLocale;

public class ModelFileWriter<DATA, CONFIG extends BaseConfig & ModelConfig> {

    private final Path exportDir;
    private final Model<DATA, CONFIG> model;
    private final String fileName;
    private final boolean overwriteExistingFiles;

    ModelFileWriter(
        Path exportDir,
        Model<DATA, CONFIG> model,
        ModelExportConfig config
    ) {
        this.exportDir = exportDir;
        this.model = model;
        this.fileName = config.fileName();
        this.overwriteExistingFiles = config.overwrite();
    }

    public void write() throws IOException {
        File metaDataFile = getOrCreateModelFile(fileName, META_DATA_SUFFIX);
        File modelDataFile = getOrCreateModelFile(fileName, MODEL_DATA_SUFFIX);
        if (!overwriteExistingFiles) {
            checkFilesExist(metaDataFile, modelDataFile);
        }

        writeDataToFile(metaDataFile, ModelMetaDataSerializer.toSerializable(model));
        writeDataToFile(modelDataFile, toSerializable(model.data(), model.algoType()));
    }

    private GeneratedMessageV3 toSerializable(DATA data, String algoType) throws IOException {
        switch (algoType) {
            case GraphSage.MODEL_TYPE:
                return GraphSageModelSerializer.toSerializable((ModelData) data);
            default:
                throw new IllegalArgumentException(formatWithLocale("Algo type %s was not found.", algoType));
        }
    }

    private <T extends GeneratedMessageV3> void writeDataToFile(File file, T data) throws IOException {
        try (var out = new FileOutputStream(file)) {
            data.writeTo(out);
        }
    }

    private File getOrCreateModelFile(String fileName, String suffix) {
        return exportDir.resolve(formatWithLocale("%s.%s", fileName, suffix)).toFile();
    }

    private static void checkFilesExist(File... files) throws FileAlreadyExistsException {
        for (File file : files) {
            if (file.exists()) {
                throw new FileAlreadyExistsException(file.getAbsolutePath());
            }
        }
    }
}
