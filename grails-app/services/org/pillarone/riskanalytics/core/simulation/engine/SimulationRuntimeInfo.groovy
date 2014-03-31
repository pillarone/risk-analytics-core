package org.pillarone.riskanalytics.core.simulation.engine

import org.pillarone.riskanalytics.core.simulation.SimulationState
import org.pillarone.riskanalytics.core.simulation.engine.grid.SimulationTask
import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import org.pillarone.riskanalytics.core.simulation.item.ResultConfiguration
import org.pillarone.riskanalytics.core.simulation.item.Simulation
import org.pillarone.riskanalytics.core.user.Person

class SimulationRuntimeInfo {
    private final QueueEntry queueEntry

    SimulationRuntimeInfo(QueueEntry queueEntry) {
        this.queueEntry = queueEntry
        if (!queueEntry) {
            throw new IllegalStateException('queueEntry must not be null')
        }
    }

    Simulation getSimulation() {
        queueEntry.simulationConfiguration.simulation
    }

    SimulationTask getSimulationTask() {
        queueEntry.simulationTask
    }

    Parameterization getParameterization() {
        simulation.parameterization
    }

    ResultConfiguration getResultConfiguration() {
        simulation.template
    }

    Integer getIterations() {
        simulation.numberOfIterations
    }

    Integer getPriority() {
        queueEntry.priority
    }

    Date getConfiguredAt() {
        queueEntry.offeredAt
    }

    UUID getId() {
        queueEntry.id
    }

    Integer getProgress() {
        queueEntry.simulationTask.progress
    }

    SimulationState getSimulationState() {
        queueEntry.simulationTask.simulationState
    }

    Person getOfferedBy() {
        queueEntry.offeredBy
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        SimulationRuntimeInfo that = (SimulationRuntimeInfo) o

        if (id != that.id) return false

        return true
    }

    int hashCode() {
        return id.hashCode()
    }
}
