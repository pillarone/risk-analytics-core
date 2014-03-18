package org.pillarone.riskanalytics.core.simulation.engine

import grails.util.Holders
import groovy.transform.CompileStatic
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.gridgain.grid.Grid

public class RunSimulationService {

    private static final Log LOG = LogFactory.getLog(RunSimulationService)

    def backgroundService
    Grid grid

    @CompileStatic
    public static RunSimulationService getService() {
        return Holders.grailsApplication.mainContext.getBean(RunSimulationService)
    }

    /**
     * Runs a simulation asynchronously using the background thread plugin.
     * @param runner
     *          A simulation runner which already has its pre and post simulation actions configured.
     * @param configuration
     *          A simulation configuration which defines the simulation run and output strategy
     */
    public synchronized SimulationRunner runSimulation(SimulationRunner runner, SimulationConfiguration configuration) {
        backgroundService.execute(configuration.simulation.name) {
            //don't start a transaction here, but inside SimulationRunner (problems with certain dbs.)
            runner.simulationConfiguration = configuration
            runner.start()
        }

        return runner
    }
}