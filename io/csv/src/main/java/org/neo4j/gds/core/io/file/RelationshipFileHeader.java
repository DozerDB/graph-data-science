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
package org.neo4j.gds.core.io.file;

import org.neo4j.gds.RelationshipType;
import org.neo4j.gds.api.schema.MutableRelationshipSchema;
import org.neo4j.gds.api.schema.RelationshipPropertySchema;
import org.neo4j.gds.core.io.file.csv.CsvRelationshipVisitor;
import org.neo4j.gds.utils.StringFormatting;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public record RelationshipFileHeader(String relationshipType, Set<HeaderProperty> propertyMappings) implements FileHeader<MutableRelationshipSchema, RelationshipPropertySchema> {

    @Override
    public Map<String, RelationshipPropertySchema> schemaForIdentifier(MutableRelationshipSchema schema) {
        return schema.filter(Set.of(RelationshipType.of(relationshipType()))).unionProperties();
    }

    public static RelationshipFileHeader of(String[] csvColumns, String relationshipType) {
        if (csvColumns.length == 0 || !csvColumns[0].equals(CsvRelationshipVisitor.START_ID_COLUMN_NAME)) {
            throw new IllegalArgumentException(StringFormatting.formatWithLocale("First column of header must be %s.", CsvRelationshipVisitor.START_ID_COLUMN_NAME));
        }
        if (csvColumns.length == 1 || !csvColumns[1].equals(CsvRelationshipVisitor.END_ID_COLUMN_NAME)) {
            throw new IllegalArgumentException(StringFormatting.formatWithLocale("Second column of header must be %s.", CsvRelationshipVisitor.END_ID_COLUMN_NAME));
        }
        var propertyMappings = new HashSet<HeaderProperty>();
        for (int i = 2; i < csvColumns.length; i++) {
            propertyMappings.add(HeaderProperty.parse(i, csvColumns[i]));
        }
        return new RelationshipFileHeader(relationshipType, propertyMappings);
    }
}
