package org.pillarone.riskanalytics.core.simulation.engine

import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.gridgain.grid.Grid
import org.pillarone.riskanalytics.core.simulation.item.ModelStructure
import org.pillarone.riskanalytics.core.simulation.item.ResultConfiguration
import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import org.pillarone.riskanalytics.core.simulation.item.Simulation
import org.pillarone.riskanalytics.core.simulation.engine.grid.SimulationTask
import org.gridgain.grid.GridTaskFuture
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.model.ModelHelper
import org.pillarone.riskanalytics.core.parameterization.ParameterApplicator

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
    public def runSimulationOnGrid(SimulationConfiguration configuration) {
        configuration.mappingCache = createMappingCache(configuration)
        configuration.prepareSimulationForGrid()

        long time = System.currentTimeMillis()

        GridTaskFuture future = grid.execute(new SimulationTask(), configuration)
        Object result = future.get()

        LOG.info "Grid task executed in ${System.currentTimeMillis() - time}ms"

        return result
    }

    /**
     * Determines all possible path & field values for this simulation and persists them if they do not exist yet, because we do not have any DB access
     * during a grid job.
     * @param simulationConfiguration the simulation details
     * @return a mapping cache filled with all necessary mappings for this simulation.
     */
    private MappingCache createMappingCache(SimulationConfiguration simulationConfiguration) {
        Model model = simulationConfiguration.simulation.modelClass.newInstance()
        model.init()

        ParameterApplicator applicator = new ParameterApplicator(model: model, parameterization: simulationConfiguration.simulation.parameterization)
        applicator.init()
        applicator.applyParameterForPeriod(0)

        Set paths = ModelHelper.getAllPossibleOutputPaths(model)
        Set fields = ModelHelper.getAllPossibleFields(model)
        MappingCache cache = new MappingCache()
        cache.initCache(model)

        for (String path in paths) {
            cache.lookupPath(path)
        }

        for (String field in fields) {
            cache.lookupField(field)
        }

        return cache
    }


}