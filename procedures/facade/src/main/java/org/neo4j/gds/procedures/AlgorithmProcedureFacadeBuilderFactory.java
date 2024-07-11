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
package org.neo4j.gds.procedures;

import org.neo4j.gds.algorithms.AlgorithmMemoryValidationService;
import org.neo4j.gds.algorithms.estimation.AlgorithmEstimator;
import org.neo4j.gds.algorithms.runner.AlgorithmRunner;
import org.neo4j.gds.api.AlgorithmMetaDataSetter;
import org.neo4j.gds.api.ProcedureReturnColumns;
import org.neo4j.gds.applications.ApplicationsFacade;
import org.neo4j.gds.applications.algorithms.machinery.RequestScopedDependencies;
import org.neo4j.gds.applications.algorithms.machinery.WriteContext;
import org.neo4j.gds.applications.algorithms.machinery.WriteNodePropertyService;
import org.neo4j.gds.configuration.DefaultsConfiguration;
import org.neo4j.gds.configuration.LimitsConfiguration;
import org.neo4j.gds.core.loading.GraphStoreCatalogService;
import org.neo4j.gds.logging.Log;
import org.neo4j.gds.memest.DatabaseGraphStoreEstimationService;
import org.neo4j.gds.memest.FictitiousGraphStoreEstimationService;
import org.neo4j.gds.metrics.algorithms.AlgorithmMetricsService;
import org.neo4j.gds.procedures.algorithms.configuration.ConfigurationCreator;
import org.neo4j.gds.procedures.algorithms.configuration.ConfigurationParser;
import org.neo4j.gds.procedures.algorithms.runners.DefaultAlgorithmExecutionScaffolding;
import org.neo4j.gds.procedures.algorithms.runners.EstimationModeRunner;
import org.neo4j.gds.procedures.algorithms.runners.MetadataSetter;
import org.neo4j.gds.procedures.algorithms.stubs.GenericStub;
import org.neo4j.kernel.api.KernelTransaction;

public class AlgorithmProcedureFacadeBuilderFactory {
    // dull utilities
    private final FictitiousGraphStoreEstimationService fictitiousGraphStoreEstimationService = new FictitiousGraphStoreEstimationService();

    // Global state and services
    private final Log log;
    private final DefaultsConfiguration defaultsConfiguration;
    private final LimitsConfiguration limitsConfiguration;
    private final GraphStoreCatalogService graphStoreCatalogService;
    private final boolean useMaxMemoryEstimation;

    // Request scoped state and services
    private final AlgorithmMetricsService algorithmMetricsService;

    public AlgorithmProcedureFacadeBuilderFactory(
        Log log,
        DefaultsConfiguration defaultsConfiguration,
        LimitsConfiguration limitsConfiguration,
        GraphStoreCatalogService graphStoreCatalogService,
        boolean useMaxMemoryEstimation,
        AlgorithmMetricsService algorithmMetricsService
    ) {
        this.log = log;
        this.defaultsConfiguration = defaultsConfiguration;
        this.limitsConfiguration = limitsConfiguration;
        this.graphStoreCatalogService = graphStoreCatalogService;
        this.useMaxMemoryEstimation = useMaxMemoryEstimation;

        this.algorithmMetricsService = algorithmMetricsService;
    }

    AlgorithmProcedureFacadeBuilder create(
        ConfigurationParser configurationParser,
        ConfigurationCreator configurationCreator,
        RequestScopedDependencies requestScopedDependencies,
        KernelTransaction kernelTransaction,
        AlgorithmMetaDataSetter algorithmMetaDataSetter,
        ApplicationsFacade applicationsFacade,
        WriteContext writeContext,
        ProcedureReturnColumns procedureReturnColumns
    ) {
        /*
         * GDS services derived from Procedure Context.
         * These come in layers, we can create some services readily,
         * but others need some of our own products and come later.
         * I have tried to mark those layers in comments below.
         */
        var algorithmMemoryValidationService = new AlgorithmMemoryValidationService(log, useMaxMemoryEstimation);
        var nodeLookup = new TransactionNodeLookup(kernelTransaction);

        // Second layer
        var writeNodePropertyService = new WriteNodePropertyService(log, requestScopedDependencies, writeContext);

        // Third layer
        var databaseGraphStoreEstimationService = new DatabaseGraphStoreEstimationService(
            requestScopedDependencies.getGraphLoaderContext(),
            requestScopedDependencies.getUser()
        );
        var algorithmEstimator = new AlgorithmEstimator(
            graphStoreCatalogService,
            fictitiousGraphStoreEstimationService,
            databaseGraphStoreEstimationService,
            requestScopedDependencies
        );
        var algorithmRunner = new AlgorithmRunner(
            log,
            graphStoreCatalogService,
            algorithmMetricsService,
            algorithmMemoryValidationService,
            requestScopedDependencies
        );

        var closeableResourceRegistry = new TransactionCloseableResourceRegistry(kernelTransaction);

        var genericStub = GenericStub.create(
            defaultsConfiguration,
            limitsConfiguration,
            graphStoreCatalogService,
            configurationCreator,
            configurationParser,
            requestScopedDependencies
        );

        var estimationModeRunner = new EstimationModeRunner(configurationCreator);
        var algorithmExecutionScaffolding = new DefaultAlgorithmExecutionScaffolding(configurationCreator);
        var algorithmExecutionScaffoldingForStreamMode = new MetadataSetter(
            algorithmMetaDataSetter,
            algorithmExecutionScaffolding
        );

        return new AlgorithmProcedureFacadeBuilder(
            requestScopedDependencies,
            configurationCreator,
            closeableResourceRegistry,
            nodeLookup,
            procedureReturnColumns,
            writeNodePropertyService,
            algorithmRunner,
            algorithmEstimator,
            applicationsFacade,
            genericStub,
            estimationModeRunner,
            algorithmExecutionScaffolding,
            algorithmExecutionScaffoldingForStreamMode
        );
    }
}
