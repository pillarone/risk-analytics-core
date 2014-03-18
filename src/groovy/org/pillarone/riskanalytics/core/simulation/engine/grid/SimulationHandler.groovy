package org.pillarone.riskanalytics.core.simulation.engine.grid

import grails.util.Holders
import groovy.transform.CompileStatic
import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.simulation.SimulationState
import org.pillarone.riskanalytics.core.simulation.engine.SimulationQueueService
import org.pillarone.riskanalytics.core.simulation.item.Simulation

@CompileStatic
class SimulationHandler {

    SimulationTask simulationTask

    void cancel() {
        Holders.grailsApplication.mainContext.getBean('simulationQueueService', SimulationQueueService).cancel(this)
    }

    int getProgress() {
        simulationTask.progress
    }

    SimulationState getSimulationState() {
        return simulationTask.simulationState
    }

    Simulation getSimulation() {
        return simulationTask.simulation
    }

    List<Throwable> getSimulationErrors() {
        return simulationTask.simulationErrors
    }

    DateTime getEstimatedSimulationEnd() {
        return simulationTask.estimatedSimulationEnd
    }
}
