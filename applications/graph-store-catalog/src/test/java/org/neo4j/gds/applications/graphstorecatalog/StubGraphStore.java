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
package org.neo4j.gds.applications.graphstorecatalog;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.neo4j.gds.NodeLabel;
import org.neo4j.gds.RelationshipType;
import org.neo4j.gds.api.CompositeRelationshipIterator;
import org.neo4j.gds.api.DatabaseId;
import org.neo4j.gds.api.DatabaseInfo;
import org.neo4j.gds.api.Graph;
import org.neo4j.gds.api.GraphStore;
import org.neo4j.gds.api.IdMap;
import org.neo4j.gds.api.Topology;
import org.neo4j.gds.api.nodeproperties.ValueType;
import org.neo4j.gds.api.properties.graph.GraphProperty;
import org.neo4j.gds.api.properties.graph.GraphPropertyValues;
import org.neo4j.gds.api.properties.nodes.NodeProperty;
import org.neo4j.gds.api.properties.nodes.NodePropertyValues;
import org.neo4j.gds.api.properties.relationships.RelationshipProperty;
import org.neo4j.gds.api.properties.relationships.RelationshipPropertyStore;
import org.neo4j.gds.api.schema.GraphSchema;
import org.neo4j.gds.core.loading.Capabilities;
import org.neo4j.gds.core.loading.DeletionResult;
import org.neo4j.gds.core.loading.SingleTypeRelationships;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Just a stub to facilitate testing
 */
class StubGraphStore implements GraphStore {
    private final DatabaseInfo databaseInfo;

    StubGraphStore(DatabaseInfo databaseInfo) {
        this.databaseInfo = databaseInfo;
    }

    StubGraphStore() {
        this(
            DatabaseInfo.of(
                DatabaseId.of(UUID.randomUUID().toString()),
                DatabaseInfo.DatabaseLocation.LOCAL
            )
        );
    }

    @Override
    public DatabaseInfo databaseInfo() {
        return databaseInfo;
    }

    @Override
    public GraphSchema schema() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public ZonedDateTime creationTime() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public ZonedDateTime modificationTime() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Capabilities capabilities() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Set<String> graphPropertyKeys() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public boolean hasGraphProperty(String propertyKey) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public GraphProperty graphProperty(String propertyKey) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public GraphPropertyValues graphPropertyValues(String propertyKey) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void addGraphProperty(String propertyKey, GraphPropertyValues propertyValues) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void removeGraphProperty(String propertyKey) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public long nodeCount() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public IdMap nodes() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Set<NodeLabel> nodeLabels() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void addNodeLabel(NodeLabel nodeLabel) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Set<String> nodePropertyKeys(NodeLabel label) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Set<String> nodePropertyKeys() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public boolean hasNodeProperty(String propertyKey) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public boolean hasNodeProperty(NodeLabel label, String propertyKey) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public boolean hasNodeProperty(Collection<NodeLabel> labels, String propertyKey) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public NodeProperty nodeProperty(String propertyKey) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void addNodeProperty(
        Set<NodeLabel> nodeLabels,
        String propertyKey,
        NodePropertyValues propertyValues
    ) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void removeNodeProperty(String propertyKey) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public long relationshipCount() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public long relationshipCount(RelationshipType relationshipType) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Set<RelationshipType> relationshipTypes() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public boolean hasRelationshipType(RelationshipType relationshipType) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Set<RelationshipType> inverseIndexedRelationshipTypes() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public boolean hasRelationshipProperty(RelationshipType relType, String propertyKey) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public ValueType relationshipPropertyType(String propertyKey) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Set<String> relationshipPropertyKeys() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Set<String> relationshipPropertyKeys(RelationshipType relationshipType) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public RelationshipProperty relationshipPropertyValues(
        RelationshipType relationshipType,
        String propertyKey
    ) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void addRelationshipType(SingleTypeRelationships relationships) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void addInverseIndex(
        RelationshipType relationshipType,
        Topology topology,
        Optional<RelationshipPropertyStore> properties
    ) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public DeletionResult deleteRelationships(RelationshipType relationshipType) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Graph getGraph(Collection<NodeLabel> nodeLabels) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Graph getGraph(
        Collection<NodeLabel> nodeLabels,
        Collection<RelationshipType> relationshipTypes,
        Optional<String> maybeRelationshipProperty
    ) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Graph getUnion() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public CompositeRelationshipIterator getCompositeRelationshipIterator(
        RelationshipType relationshipType,
        Collection<String> propertyKeys
    ) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
