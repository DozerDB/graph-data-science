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
package org.neo4j.gds.projection;

import com.carrotsearch.hppc.LongArrayList;
import com.carrotsearch.hppc.LongScatterSet;
import org.apache.commons.lang3.mutable.MutableInt;
import org.junit.jupiter.api.Test;
import org.neo4j.gds.BaseTest;
import org.neo4j.gds.compat.GraphDatabaseApiProxy;
import org.neo4j.gds.core.utils.paged.HugeAtomicBitSet;
import org.neo4j.gds.transaction.DatabaseTransactionContext;
import org.neo4j.graphdb.Label;

import static org.assertj.core.api.Assertions.assertThat;
import static org.neo4j.gds.compat.GraphDatabaseApiProxy.runInFullAccessTransaction;

class NodeLabelIndexBasedScannerTest extends BaseTest {

    @Test
    void testMultipleNodeLabels() {
        var nodeCount = 150_000;
        var prefetchSize = StoreScanner.DEFAULT_PREFETCH_SIZE;

        var labelABits = HugeAtomicBitSet.create(nodeCount);
        var labelBBits = HugeAtomicBitSet.create(nodeCount);

        var labelA = Label.label("A");
        var labelB = Label.label("B");

        runInFullAccessTransaction(db, tx -> {
            for (int i = 0; i < nodeCount; i++) {
                if (i % 3 == 0) {
                    labelABits.set(tx.createNode(labelA).getId());
                } else if (i % 3 == 1) {
                    labelBBits.set(tx.createNode(labelB).getId());
                } else {
                    var id = tx.createNode(labelA, labelB).getId();
                    labelABits.set(id);
                    labelBBits.set(id);
                }
            }
        });

        try (var transactions = GraphDatabaseApiProxy.newKernelTransaction(db)) {
            var txContext = DatabaseTransactionContext.of(db, transactions.tx());
            var ktx = transactions.ktx();

            var labelAToken = ktx.tokenRead().nodeLabel(labelA.name());
            var labelBToken = ktx.tokenRead().nodeLabel(labelB.name());

            var labelIds = new int[]{labelAToken, labelBToken};

            try (var scanner = new MultipleNodeLabelIndexBasedScanner(
                labelIds,
                prefetchSize,
                txContext
            );
                 var storeScanner = scanner.createCursor(ktx)) {

                var actualNodeCount = new MutableInt();

                var idList = new LongArrayList();
                var idSet = new LongScatterSet();


                while (storeScanner.reserveBatch() && storeScanner.consumeBatch(nodeReference -> {
                    actualNodeCount.increment();

                    var neoId = nodeReference.nodeId();
                    idList.add(neoId);
                    idSet.add(neoId);
                    var labels = nodeReference.labels();

                    if (neoId % 3 == 0) {
                        assertThat(labels.asIntArray()).contains(labelAToken);
                        assertThat(labels.asIntArray()).doesNotContain(labelBToken);
                        assertThat(labelABits.get(neoId)).isTrue();
                        assertThat(labelBBits.get(neoId)).isFalse();
                    }
                    if (neoId % 3 == 1) {
                        assertThat(labels.asIntArray()).doesNotContain(labelAToken);
                        assertThat(labels.asIntArray()).contains(labelBToken);
                        assertThat(labelABits.get(neoId)).isFalse();
                        assertThat(labelBBits.get(neoId)).isTrue();
                    }
                    if (neoId % 3 == 2) {
                        assertThat(labels.asIntArray()).contains(labelAToken);
                        assertThat(labels.asIntArray()).contains(labelBToken);
                        assertThat(labelABits.get(neoId)).isTrue();
                        assertThat(labelBBits.get(neoId)).isTrue();
                    }
                    return true;
                })) {
                }

                assertThat(idList.size()).isEqualTo(idSet.size());
                assertThat(actualNodeCount.getValue()).isEqualTo(nodeCount);
            }
        }
    }

    @Test
    void testBatchSizeAlignment() {
        var prefetchSize = StoreScanner.DEFAULT_PREFETCH_SIZE;
        // batchSize = prefetch size * PAGE_SIZE / NodeRecord size
        var expectedBatchSize = 54_656;

        var label = Label.label("Node");
        long labelCount = 2 * expectedBatchSize;

        runInFullAccessTransaction(db, tx -> {
            for (int i = 0; i < labelCount; i++) {
                tx.createNode(label);
            }
        });

        try (var transactions = GraphDatabaseApiProxy.newKernelTransaction(db)) {
            var txContext = DatabaseTransactionContext.of(db, transactions.tx());
            var ktx = transactions.ktx();
            var labelToken = ktx.tokenRead().nodeLabel(label.name());

            try (
                var scanner = new NodeLabelIndexBasedScanner(
                    labelToken,
                    prefetchSize,
                    txContext
                );
                var storeScanner = scanner.createCursor(ktx)
            ) {
                assertThat(scanner.batchSize()).isEqualTo(expectedBatchSize);

                var consumer = new StoreScanner.RecordConsumer<>() {
                    int partitionSize = 0;

                    @Override
                    public boolean offer(Object o) {
                        partitionSize++;
                        return (partitionSize < expectedBatchSize);
                    }

                    @Override
                    public void reset() {
                        partitionSize = 0;
                    }
                };

                int actualNodeCount = 0;
                var scan = ScanState.of();
                while (scan.scan(storeScanner, consumer)) {
                    assertThat(consumer.partitionSize).isLessThanOrEqualTo(expectedBatchSize);
                    actualNodeCount += consumer.partitionSize;
                }

                assertThat(actualNodeCount).isEqualTo(labelCount);
            }
        }
    }
}
