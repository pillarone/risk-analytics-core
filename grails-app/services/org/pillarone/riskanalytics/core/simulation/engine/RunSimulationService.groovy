package org.pillarone.riskanalytics.core.simulation.engine

import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.gridgain.grid.Grid

import org.pillarone.riskanalytics.core.simulation.engine.grid.SimulationTask

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.model.ModelHelper
import org.pillarone.riskanalytics.core.parameterization.ParameterApplicator

import org.pillarone.riskanalytics.core.simulation.engine.grid.SimulationHandler
import org.pillarone.riskanalytics.core.output.PacketCollector
import org.pillarone.riskanalytics.core.output.CollectingModeFactory
import org.pillarone.riskanalytics.core.output.ICollectingModeStrategy
import org.pillarone.riskanalytics.core.output.CollectorFactory
import org.pillarone.riskanalytics.core.output.AggregatedCollectingModeStrategy
import org.pillarone.riskanalytics.core.simulation.item.ResultConfiguration

public class RunSimulationService {

    private static Log LOG = LogFactory.getLog(RunSimulationService)

    def backgroundService
    Grid grid

    public static RunSimulationService getService() {
        return ApplicationHolder.getApplication().getMainContext().getBean('runSimulationService')
    }

    /**
     * Runs a simulation asynchronously using the background thread plugin.
     * @param runner
     *          A simulation runner which already has its pre and post simulation actions configured.
     * @param configuration
     *          A simulation configuration which defines the simulation run and output strategy
     */
    public synchronized SimulationRunner runSimulation(SimulationRunner runner, SimulationConfiguration configuration) {
        backgroundService.execute(configuration.simulation.name) { //don't start a transaction here, but inside SimulationRunner (problems with certain dbs.)
            runner.simulationConfiguration = configuration
            runner.start()
        }

        return runner
    }

    /**
     * Runs a simulation on a GridGain grid
     * @param configuration the simulation details
     * @return the result of the grid gain task
     */
    public SimulationHandler runSimulationOnGrid(SimulationConfiguration configuration, ResultConfiguration resultConfiguration) {
        configuration.mappingCache = createMappingCache(configuration, resultConfiguration)
        configuration.prepareSimulationForGrid()

        SimulationTask task = new SimulationTask()
        SimulationHandler handler = new SimulationHandler(simulationTask: task)
        handler.gridTaskFuture = grid.execute(task, configuration)

        return handler
    }

    /**
     * Determines all possible path & field values for this simulation and persists them if they do not exist yet, because we do not have any DB access
     * during a grid job.
     * @param simulationConfiguration the simulation details
     * @return a mapping cache filled with all necessary mappings for this simulation.
     */
    private MappingCache createMappingCache(SimulationConfiguration simulationConfiguration, ResultConfiguration resultConfiguration) {
        Model model = simulationConfiguration.simulation.modelClass.newInstance()
        model.init()

        ParameterApplicator parameterApplicator = new ParameterApplicator(model: model, parameterization: simulationConfiguration.simulation.parameterization)
        parameterApplicator.init()
        parameterApplicator.applyParameterForPeriod(0)

        SimulationRunner runner = SimulationRunner.createRunner()
        CollectorFactory collectorFactory = runner.currentScope.collectorFactory
        List<PacketCollector> drillDownCollectors = resultConfiguration.getResolvedCollectors(model, collectorFactory)
        List<String> drillDownPaths = getDrillDownPaths(drillDownCollectors)
        Set paths = ModelHelper.getAllPossibleOutputPaths(model, null)

        Set fields = ModelHelper.getAllPossibleFields(model)
        MappingCache cache = new MappingCache()
        cache.initCache(model)

        for (String path in paths) {
            cache.lookupPathDB(path)
        }

        for (String field in fields) {
            cache.lookupField(field)
        }

        return cache
    }

    private List<String> getDrillDownPaths(List<PacketCollector> collectors) {
        List<String> paths = []
        // todo: requires a proper refactoring as the core plugin itself knows nothing about the aggregate drill down collector
        ICollectingModeStrategy drillDownCollector = CollectingModeFactory.getStrategy("AGGREGATED_DRILL_DOWN")
        if (drillDownCollector != null) {
            for (PacketCollector collector : collectors) {
                if (collector.mode.class.equals(drillDownCollector.class)) {
                    paths << collector.path
                }
            }
        }
        return paths
    }

}