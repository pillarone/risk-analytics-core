package org.pillarone.riskanalytics.core.simulation.engine

import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.pillarone.riskanalytics.core.ParameterizationDAO

public class RunSimulationService {

    def backgroundService

    public static RunSimulationService getService() {
        return ApplicationHolder.getApplication().getMainContext().getBean('runSimulationService')
    }

    public synchronized SimulationRunner runSimulation(SimulationRunner runner, SimulationConfiguration configuration) {
        backgroundService.execute(configuration.simulationRun.name) {
            ParameterizationDAO.withTransaction {status ->
                runner.simulationConfiguration = configuration
                runner.start()
            }
        }

        return runner
    }
}