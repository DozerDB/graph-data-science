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
package org.neo4j.gds.compat._524;

import org.neo4j.batchimport.api.IndexConfig;
import org.neo4j.configuration.Config;
import org.neo4j.gds.compat.batchimport.BatchImporter;
import org.neo4j.gds.compat.batchimport.ImportConfig;
import org.neo4j.gds.compat.batchimport.InputIterable;
import org.neo4j.gds.compat.batchimport.InputIterator;
import org.neo4j.gds.compat.batchimport.Monitor;
import org.neo4j.gds.compat.batchimport.input.Collector;
import org.neo4j.gds.compat.batchimport.input.Estimates;
import org.neo4j.gds.compat.batchimport.input.Group;
import org.neo4j.gds.compat.batchimport.input.IdType;
import org.neo4j.gds.compat.batchimport.input.Input;
import org.neo4j.gds.compat.batchimport.input.InputChunk;
import org.neo4j.gds.compat.batchimport.input.InputEntityVisitor;
import org.neo4j.gds.compat.batchimport.input.ReadableGroups;
import org.neo4j.internal.batchimport.DefaultAdditionalIds;
import org.neo4j.internal.batchimport.input.Collectors;
import org.neo4j.internal.batchimport.input.Groups;
import org.neo4j.io.fs.FileSystemAbstraction;
import org.neo4j.io.layout.DatabaseLayout;
import org.neo4j.io.pagecache.context.CursorContextFactory;
import org.neo4j.io.pagecache.tracing.PageCacheTracer;
import org.neo4j.kernel.impl.index.schema.IndexImporterFactoryImpl;
import org.neo4j.kernel.impl.transaction.log.files.TransactionLogInitializer;
import org.neo4j.logging.internal.LogService;
import org.neo4j.memory.EmptyMemoryTracker;
import org.neo4j.scheduler.JobScheduler;
import org.neo4j.storageengine.api.StorageEngineFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public final class BatchImporterCompat {

    private BatchImporterCompat() {}

    static BatchImporter instantiateBatchImporter(
        DatabaseLayout dbLayout,
        FileSystemAbstraction fileSystem,
        ImportConfig config,
        Monitor monitor,
        LogService logService,
        Config dbConfig,
        JobScheduler jobScheduler,
        Collector badCollector
    ) {
        var storageEngineFactory = StorageEngineFactory.selectStorageEngine(dbConfig);
        var progressOutput = new PrintStream(PrintStream.nullOutputStream(), true, StandardCharsets.UTF_8);
        var verboseProgressOutput = false;

        var importer = storageEngineFactory.batchImporter(
            dbLayout,
            fileSystem,
            PageCacheTracer.NULL,
            new ConfigurationAdapter(config),
            logService,
            progressOutput,
            verboseProgressOutput,
            DefaultAdditionalIds.EMPTY,
            dbConfig,
            new MonitorAdapter(monitor),
            jobScheduler,
            badCollector != null ? ((CollectorAdapter) badCollector).delegate : null,
            TransactionLogInitializer.getLogFilesInitializer(),
            new IndexImporterFactoryImpl(),
            EmptyMemoryTracker.INSTANCE,
            CursorContextFactory.NULL_CONTEXT_FACTORY
        );
        return new BatchImporterReverseAdapter(importer);
    }

    private static final class MonitorAdapter implements org.neo4j.batchimport.api.Monitor {
        private final Monitor delegate;

        private MonitorAdapter(Monitor delegate) {this.delegate = delegate;}

        @Override
        public void started() {
            delegate.started();
        }

        @Override
        public void percentageCompleted(int percentage) {
            delegate.percentageCompleted(percentage);
        }

        @Override
        public void completed(boolean success) {
            delegate.completed(success);
        }
    }

    static final class ConfigurationAdapter implements org.neo4j.batchimport.api.Configuration {
        private final ImportConfig inner;

        ConfigurationAdapter(ImportConfig inner) {
            this.inner = inner;
        }

        @Override
        public int batchSize() {
            return this.inner.batchSize();
        }

        @Override
        public int maxNumberOfWorkerThreads() {
            return this.inner.writeConcurrency();
        }

        @Override
        public boolean highIO() {
            return this.inner.highIO();
        }

        @Override
        public IndexConfig indexConfig() {
            var config = IndexConfig.DEFAULT;
            if (this.inner.createLabelIndex()) {
                config = config.withLabelIndex();
            }
            if (this.inner.createRelationshipTypeIndex()) {
                config = config.withRelationshipTypeIndex();
            }
            return config;
        }
    }

    static final class BatchImporterReverseAdapter implements BatchImporter {
        private final org.neo4j.batchimport.api.BatchImporter delegate;

        BatchImporterReverseAdapter(org.neo4j.batchimport.api.BatchImporter delegate) {this.delegate = delegate;}

        @Override
        public void doImport(Input input) throws IOException {
            delegate.doImport(new InputAdapter(input));
        }
    }

    static final class InputAdapter implements org.neo4j.batchimport.api.input.Input {
        private final Input delegate;

        InputAdapter(Input delegate) {this.delegate = delegate;}

        @Override
        public org.neo4j.batchimport.api.InputIterable nodes(org.neo4j.batchimport.api.input.Collector collector) {
            return new InputIterableAdapter(delegate.nodes());
        }

        @Override
        public org.neo4j.batchimport.api.InputIterable relationships(org.neo4j.batchimport.api.input.Collector collector) {
            return new InputIterableAdapter(delegate.relationships());
        }

        @Override
        public org.neo4j.batchimport.api.input.IdType idType() {
            return adaptIdType(delegate.idType());
        }

        @Override
        public org.neo4j.batchimport.api.input.ReadableGroups groups() {
            return adaptReadableGroups(delegate.groups());
        }

        @Override
        public Estimates calculateEstimates(org.neo4j.batchimport.api.input.PropertySizeCalculator propertySizeCalculator)
        throws IOException {
            return adaptEstimates(delegate.calculateEstimates());
        }

        @Override
        public void close() {
            delegate.close();
        }
    }

    static final class InputIterableAdapter implements org.neo4j.batchimport.api.InputIterable {
        private final InputIterable delegate;

        InputIterableAdapter(InputIterable delegate) {this.delegate = delegate;}

        @Override
        public org.neo4j.batchimport.api.InputIterator iterator() {
            return new InputIteratorAdapter(delegate.iterator());
        }
    }

    static final class InputIteratorAdapter implements org.neo4j.batchimport.api.InputIterator {
        private final InputIterator delegate;

        InputIteratorAdapter(InputIterator delegate) {this.delegate = delegate;}

        @Override
        public org.neo4j.batchimport.api.input.InputChunk newChunk() {
            return new InputChunkAdapter(delegate.newChunk());
        }

        @Override
        public boolean next(org.neo4j.batchimport.api.input.InputChunk inputChunk) throws IOException {
            return delegate.next(((InputChunkAdapter) inputChunk).delegate);
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }
    }

    private static final class InputChunkAdapter implements org.neo4j.batchimport.api.input.InputChunk {
        private final InputChunk delegate;

        private InputChunkAdapter(InputChunk delegate) {this.delegate = delegate;}

        @Override
        public boolean next(org.neo4j.batchimport.api.input.InputEntityVisitor inputEntityVisitor) throws
            IOException {
            return delegate.next(new InputEntityVisitorReverseAdapter(inputEntityVisitor));
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }
    }

    private static final class InputEntityVisitorReverseAdapter implements InputEntityVisitor {
        private final org.neo4j.batchimport.api.input.InputEntityVisitor delegate;

        InputEntityVisitorReverseAdapter(org.neo4j.batchimport.api.input.InputEntityVisitor delegate) {this.delegate = delegate;}

        @Override
        public boolean property(String s, Object o) {
            return delegate.property(s, o);
        }

        @Override
        public boolean id(long l) {
            return delegate.id(l);
        }

        @Override
        public boolean id(long id, Group group) {
            return delegate.id(id, ((GroupReverseAdapter) group).delegate);
        }

        @Override
        public boolean id(String id, Group group) {
            return delegate.id(id, ((GroupReverseAdapter) group).delegate);
        }

        @Override
        public boolean labels(String[] strings) {
            return delegate.labels(strings);
        }

        @Override
        public boolean startId(long l) {
            return delegate.startId(l);
        }

        @Override
        public boolean startId(long id, Group group) {
            return delegate.startId(id, ((GroupReverseAdapter) group).delegate);
        }

        @Override
        public boolean startId(String id, Group group) {
            return delegate.startId(id, ((GroupReverseAdapter) group).delegate);
        }

        @Override
        public boolean endId(long l) {
            return delegate.endId(l);
        }

        @Override
        public boolean endId(long id, Group group) {
            return delegate.endId(id, ((GroupReverseAdapter) group).delegate);
        }

        @Override
        public boolean endId(String id, Group group) {
            return delegate.endId(id, ((GroupReverseAdapter) group).delegate);
        }

        @Override
        public boolean type(String s) {
            return delegate.type(s);
        }

        @Override
        public void endOfEntity() throws IOException {
            delegate.endOfEntity();
        }

        @Override
        public void reset() {
            delegate.reset();
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }
    }

    private static Group adaptGroup(org.neo4j.batchimport.api.input.Group group) {
        return new GroupReverseAdapter(group);
    }

    private static final class GroupReverseAdapter implements Group {
        private final org.neo4j.batchimport.api.input.Group delegate;

        private GroupReverseAdapter(org.neo4j.batchimport.api.input.Group delegate) {this.delegate = delegate;}
    }

    private static org.neo4j.batchimport.api.input.IdType adaptIdType(IdType idType) {
        return switch (idType) {
            case ACTUAL -> org.neo4j.batchimport.api.input.IdType.ACTUAL;
            case INTEGER -> org.neo4j.batchimport.api.input.IdType.INTEGER;
            case STRING -> org.neo4j.batchimport.api.input.IdType.STRING;
        };
    }

    private static org.neo4j.batchimport.api.input.ReadableGroups adaptReadableGroups(ReadableGroups groups) {
        return groups == null || groups == ReadableGroups.EMPTY
            ? org.neo4j.batchimport.api.input.ReadableGroups.EMPTY
            : ((ReadableGroupsReverseAdapter) groups).delegate;
    }

    private static ReadableGroups adaptReadableGroups(org.neo4j.batchimport.api.input.ReadableGroups groups) {
        return groups == null || groups == org.neo4j.batchimport.api.input.ReadableGroups.EMPTY
            ? ReadableGroups.EMPTY
            : new ReadableGroupsReverseAdapter(groups);
    }

    private static class ReadableGroupsReverseAdapter implements ReadableGroups {
        private final org.neo4j.batchimport.api.input.ReadableGroups delegate;

        ReadableGroupsReverseAdapter(org.neo4j.batchimport.api.input.ReadableGroups delegate) {this.delegate = delegate;}

        @Override
        public Group getGlobalGroup() {
            return adaptGroup(delegate.get(null));
        }
    }

    private static org.neo4j.batchimport.api.input.Input.Estimates adaptEstimates(Estimates estimates) {
        return estimates == null || estimates == Estimates.NULL
            ? NULL_ESTIMATES
            : ((EstimatesAdapter) estimates).delegate;
    }


    static ReadableGroups newGroups() {
        return adaptReadableGroups(new Groups());
    }

    static ReadableGroups newInitializedGroups() {
        var groups = new Groups();
        groups.getOrCreate(null);
        return adaptReadableGroups(groups);
    }

    static Collector emptyCollector() {
        return CollectorAdapter.EMPTY;
    }

    static Collector badCollector(OutputStream outputStream, int batchSize) {
        return new CollectorAdapter(Collectors.badCollector(outputStream, batchSize));
    }

    private static final class CollectorAdapter implements Collector {
        private static final Collector EMPTY = new CollectorAdapter(org.neo4j.batchimport.api.input.Collector.EMPTY);

        private final org.neo4j.batchimport.api.input.Collector delegate;

        CollectorAdapter(org.neo4j.batchimport.api.input.Collector delegate) {
            this.delegate = delegate;
        }
    }

    private static final org.neo4j.batchimport.api.input.Input.Estimates NULL_ESTIMATES =
        org.neo4j.batchimport.api.input.Input.knownEstimates(0, 0, 0, 0, 0, 0, 0);

    static Estimates knownEstimates(
        long numberOfNodes,
        long numberOfRelationships,
        long numberOfNodeProperties,
        long numberOfRelationshipProperties,
        long sizeOfNodeProperties,
        long sizeOfRelationshipProperties,
        long numberOfNodeLabels
    ) {
        if (numberOfNodes == 0 && numberOfRelationships == 0 && numberOfNodeProperties == 0
            && numberOfRelationshipProperties == 0 && sizeOfNodeProperties == 0 && sizeOfRelationshipProperties == 0
            && numberOfNodeLabels == 0) {
            return Estimates.NULL;
        }
        return new EstimatesAdapter(org.neo4j.batchimport.api.input.Input.knownEstimates(
            numberOfNodes,
            numberOfRelationships,
            numberOfNodeProperties,
            numberOfRelationshipProperties,
            sizeOfNodeProperties,
            sizeOfRelationshipProperties,
            numberOfNodeLabels
        ));
    }

    private static final class EstimatesAdapter implements Estimates {
        private final org.neo4j.batchimport.api.input.Input.Estimates delegate;

        EstimatesAdapter(org.neo4j.batchimport.api.input.Input.Estimates delegate) {
            this.delegate = delegate;
        }
    }
}
