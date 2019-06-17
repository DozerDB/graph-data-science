/*
 * Copyright (c) 2017-2019 "Neo4j,"
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
package org.neo4j.graphalgo.core;

import org.neo4j.graphalgo.PropertyMapping;
import org.neo4j.graphalgo.api.GraphSetup;
import org.neo4j.internal.kernel.api.Read;
import org.neo4j.internal.kernel.api.TokenRead;

import static org.neo4j.internal.kernel.api.Read.ANY_LABEL;

public final class GraphDimensions {

    private final long nodeCount;
    private final long allNodesCount;
    private final long maxRelCount;
    private final int labelId;
    private final int[] relationId;
    private final int relWeightId;

    private final int nodeWeightId;
    private final int nodePropId;
    private final int[] nodePropIds;

    public GraphDimensions(
            final long nodeCount,
            final long allNodesCount,
            final long maxRelCount,
            final int labelId,
            final int[] relationId,
            final int relWeightId,
            final int nodeWeightId, final int nodePropId, final int[] nodePropIds) {
        this.nodeCount = nodeCount;
        this.allNodesCount = allNodesCount;
        this.maxRelCount = maxRelCount;
        this.labelId = labelId;
        this.relationId = relationId;
        this.relWeightId = relWeightId;
        this.nodeWeightId = nodeWeightId;
        this.nodePropId = nodePropId;
        this.nodePropIds = nodePropIds;
    }


    public long hugeNodeCount() {
        return nodeCount;
    }

    public long allNodesCount() {
        return allNodesCount;
    }

    public int nodeCount() {
        return Math.toIntExact(nodeCount);
    }

    public long maxRelCount() {
        return maxRelCount;
    }

    public int labelId() {
        return labelId;
    }

    public int[] relationshipTypeId() {
        return relationId;
    }

    public int singleRelationshipTypeId() {
        return relationId == null ? Read.ANY_RELATIONSHIP_TYPE : relationId[0];
    }

    public int relWeightId() {
        return relWeightId;
    }

    public int nodeWeightId() {
        return nodeWeightId;
    }

    public int nodePropId() {
        return nodePropId;
    }

    public int nodePropertyKeyId(String type, GraphSetup setup) {
        PropertyMapping[] mappings = setup.nodePropertyMappings;

        for (int i = 0; i < mappings.length; i++) {
            if (mappings[i].propertyName.equals(type)) {
                return nodePropIds[i];
            }
        }
        return -1;
    }

    public int nodePropertyKeyId(int mappingIndex) {
        if (mappingIndex < 0 || mappingIndex >= nodePropIds.length) {
            return TokenRead.NO_TOKEN;
        }
        return nodePropIds[mappingIndex];
    }

    public double nodePropertyDefaultValue(String type, GraphSetup setup) {
        PropertyMapping[] mappings = setup.nodePropertyMappings;

        for (PropertyMapping mapping : mappings) {
            if (mapping.propertyName.equals(type)) {
                return mapping.defaultValue;
            }
        }
        return 0.0;
    }

    public boolean isValidNodePredicate(GraphSetup setup) {
        return (setup.startLabel == null || setup.startLabel.isEmpty()) || labelId() != ANY_LABEL;
    }

    public boolean isValidRelationshipTypePredicate(GraphSetup setup) {
        return (setup.relationshipType == null || setup.relationshipType.isEmpty()) || singleRelationshipTypeId() != ANY_LABEL;
    }


    public static class Builder {
        private long nodeCount;
        private long allNodesCount;
        private long maxRelCount;
        private int labelId;
        private int[] relationId;
        private int relWeightId;
        private int nodeWeightId;
        private int nodePropId;
        private int[] nodePropIds;

        public Builder setNodeCount(final long nodeCount) {
            this.nodeCount = nodeCount;
            return this;
        }

        public Builder setAllNodesCount(final long allNodesCount) {
            this.allNodesCount = allNodesCount;
            return this;
        }

        public Builder setMaxRelCount(final long maxRelCount) {
            this.maxRelCount = maxRelCount;
            return this;
        }

        public Builder setLabelId(final int labelId) {
            this.labelId = labelId;
            return this;
        }

        public Builder setRelationId(final int[] relationId) {
            this.relationId = relationId;
            return this;
        }

        public Builder setRelWeightId(final int relWeightId) {
            this.relWeightId = relWeightId;
            return this;
        }

        public Builder setNodeWeightId(final int nodeWeightId) {
            this.nodeWeightId = nodeWeightId;
            return this;
        }

        public Builder setNodePropId(final int nodePropId) {
            this.nodePropId = nodePropId;
            return this;
        }

        public Builder setNodePropIds(final int[] nodePropIds) {
            this.nodePropIds = nodePropIds;
            return this;
        }

        public GraphDimensions build() {
            return new GraphDimensions(
                    nodeCount,
                    allNodesCount,
                    maxRelCount,
                    labelId,
                    relationId,
                    relWeightId,
                    nodeWeightId,
                    nodePropId,
                    nodePropIds);
        }
    }
}
