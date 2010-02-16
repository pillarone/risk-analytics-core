package org.pillarone.riskanalytics.core.simulation.engine

import org.codehaus.groovy.grails.commons.ApplicationHolder

public class RunSimulationService {

    def backgroundService

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
}