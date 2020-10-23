/*
 * Copyright (c) 2017-2020 "Neo4j,"
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
package org.neo4j.graphalgo.core.loading;

import org.neo4j.graphalgo.NodeLabel;
import org.neo4j.graphalgo.PropertyMapping;
import org.neo4j.graphalgo.api.NodeProperties;

import java.util.Map;

public class IdsAndProperties {

    final IdMap idMap;
    private final Map<NodeLabel, Map<PropertyMapping, NodeProperties>> properties;

    public static IdsAndProperties of(
        IdMap hugeIdMap,
        Map<NodeLabel, Map<PropertyMapping, NodeProperties>> properties
    ) {
        return new IdsAndProperties(
            hugeIdMap,
            properties
        );
    }


    public IdsAndProperties(IdMap idMap, Map<NodeLabel, Map<PropertyMapping, NodeProperties>> properties) {
        this.idMap = idMap;
        this.properties = properties;
    }

    public IdMap idMap() {
        return idMap;
    }

    public Map<NodeLabel, Map<PropertyMapping, NodeProperties>> properties() {
        return properties;
    }
}
