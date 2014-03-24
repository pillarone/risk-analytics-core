package org.pillarone.riskanalytics.core.simulation.engine

import org.pillarone.riskanalytics.core.simulation.engine.grid.SimulationTask

class QueueEntry implements Comparable<QueueEntry> {
    final UUID id
    final Date offeredAt
    int priority
    final SimulationTask simulationTask
    final SimulationConfiguration simulationConfiguration

    QueueEntry(SimulationTask simulationTask, SimulationConfiguration simulationConfiguration, int priority) {
        this.priority = priority
        this.simulationTask = simulationTask
        this.simulationConfiguration = simulationConfiguration
        id = UUID.randomUUID()
        offeredAt = new Date()
    }

    QueueEntry(UUID id) {
        this.id = id
        this.priority = 0
        this.simulationTask = null
        this.simulationConfiguration = null
        offeredAt = null
    }

    int compareTo(QueueEntry o) {
        if (priority.equals(o.priority)) {
            offeredAt.compareTo(o.offeredAt)
        }
        return priority.compareTo(o.priority)
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        QueueEntry that = (QueueEntry) o

        if (id != that.id) return false

        return true
    }

    int hashCode() {
        return id.hashCode()
    }
}