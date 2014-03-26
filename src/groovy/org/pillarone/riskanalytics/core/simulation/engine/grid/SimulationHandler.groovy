package org.pillarone.riskanalytics.core.simulation.engine.grid

import grails.util.Holders
import groovy.transform.CompileStatic
import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.simulation.SimulationState
import org.pillarone.riskanalytics.core.simulation.engine.SimulationQueueService
import org.pillarone.riskanalytics.core.simulation.item.Simulation

@CompileStatic
class SimulationHandler {

    private SimulationTask simulationTask
    private UUID id

    SimulationHandler(SimulationTask simulationTask, UUID id) {
        this.simulationTask = simulationTask
        this.id = id
    }

    void cancel() {
        Holders.grailsApplication.mainContext.getBean('simulationQueueService', SimulationQueueService).cancel(id)
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
