package org.pillarone.riskanalytics.core.simulation.engine.grid

import org.gridgain.grid.GridTaskFuture
import org.pillarone.riskanalytics.core.simulation.SimulationState
import org.pillarone.riskanalytics.core.simulation.item.Simulation
import org.joda.time.DateTime


class SimulationHandler {

    SimulationTask simulationTask
    GridTaskFuture gridTaskFuture

    void cancel() {
        simulationTask.cancel()
        gridTaskFuture.cancel()
    }

    int getProgress() {
        simulationTask.getProgress()
    }

    SimulationState getSimulationState() {
        return simulationTask.getSimulationState()
    }

    Simulation getSimulation() {
        return simulationTask.simulation
    }

    List<Throwable> getSimulationErrors() {
        return simulationTask.getSimulationErrors()
    }

    DateTime getEstimatedSimulationEnd() {
        return simulationTask.getEstimatedSimulationEnd()
    }
}
