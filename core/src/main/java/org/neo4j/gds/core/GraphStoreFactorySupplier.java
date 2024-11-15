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
package org.neo4j.gds.core;

import org.neo4j.gds.api.GraphStoreFactory;
import org.neo4j.gds.api.GraphStoreFactorySupplierProvider;
import org.neo4j.gds.config.GraphProjectConfig;

import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import static org.neo4j.gds.utils.StringFormatting.formatWithLocale;

public final class GraphStoreFactorySupplier {

    private GraphStoreFactorySupplier() {}

    private static final List<GraphStoreFactorySupplierProvider> PROVIDERS;

    static {
        PROVIDERS = ServiceLoader
            .load(GraphStoreFactorySupplierProvider.class, GraphStoreFactorySupplierProvider.class.getClassLoader())
            .stream()
            .map(ServiceLoader.Provider::get)
            .collect(Collectors.toList());
    }

    public static GraphStoreFactory.Supplier supplier(GraphProjectConfig graphProjectConfig) {
        return PROVIDERS.stream()
            .filter(f -> f.canSupplyFactoryFor(graphProjectConfig))
            .findFirst()
            .orElseThrow(() -> new UnsupportedOperationException(formatWithLocale(
                "%s does not support GraphStoreFactory creation",
                graphProjectConfig.getClass().getName()
            )))
            .supplier(graphProjectConfig);
    }
}
