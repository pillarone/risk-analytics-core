package org.pillarone.riskanalytics.core.simulation.engine.grid

import groovy.transform.CompileStatic
import org.gridgain.grid.GridTaskFuture
import org.pillarone.riskanalytics.core.simulation.SimulationState
import org.pillarone.riskanalytics.core.simulation.item.Simulation
import org.joda.time.DateTime

@CompileStatic
class SimulationHandler {

    SimulationTask simulationTask
    GridTaskFuture gridTaskFuture

    void cancel() {
        simulationTask.cancel()
        gridTaskFuture.cancel()
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
