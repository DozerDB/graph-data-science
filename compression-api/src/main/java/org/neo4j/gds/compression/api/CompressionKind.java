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
package org.neo4j.gds.compression.api;

public record CompressionKind(String name) {
    public static final CompressionKind DELTA_VAR_LONG  = new CompressionKind("DELTA_VAR_LONG");
    public static final CompressionKind PACKED           = new CompressionKind("PACKED");
    public static final CompressionKind UNCOMPRESSED     = new CompressionKind("UNCOMPRESSED");
    public static final CompressionKind MIXED            = new CompressionKind("MIXED");
}
