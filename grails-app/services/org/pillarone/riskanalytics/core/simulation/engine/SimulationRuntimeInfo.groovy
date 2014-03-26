package org.pillarone.riskanalytics.core.simulation.engine

import org.pillarone.riskanalytics.core.simulation.SimulationState
import org.pillarone.riskanalytics.core.simulation.item.Simulation

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

    String getP14n() {
        simulation.parameterization?.nameAndVersion
    }

    String getResultConfiguration() {
        simulation.template?.nameAndVersion
    }

    Integer getIterations() {
        simulation.numberOfIterations
    }

    Integer getPriority() {
        queueEntry.priority
    }

    String getConfiguredAt() {
        //TODO format
        queueEntry.offeredAt.toString()
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

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        SimulationRuntimeInfo that = (SimulationRuntimeInfo) o

        if (queueEntry != that.queueEntry) return false

        return true
    }

    int hashCode() {
        return queueEntry.hashCode()
    }
}
