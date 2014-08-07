package org.pillarone.riskanalytics.core.simulation.engine

import org.pillarone.riskanalytics.core.queue.IQueueEntry
import org.pillarone.riskanalytics.core.simulation.engine.grid.SimulationTask
import org.pillarone.riskanalytics.core.user.Person

class QueueEntry implements IQueueEntry<SimulationResult> {
    final UUID id
    final Date offeredAt
    int priority
    final SimulationTask simulationTask
    final Person offeredBy
    final long offeredNanoTime
    SimulationResult result

    QueueEntry(SimulationConfiguration simulationConfiguration, int priority, Person offeredBy) {
        this.simulationTask = new SimulationTask(simulationConfiguration: simulationConfiguration)
        this.offeredBy = offeredBy
        this.priority = priority
        id = UUID.randomUUID()
        offeredAt = new Date()
        offeredNanoTime = System.nanoTime()
    }

    QueueEntry(UUID id) {
        this.id = id
        this.priority = 0
        this.simulationTask = null
        this.offeredBy = null
        offeredAt = null
        offeredNanoTime = System.nanoTime()
    }

    int compareTo(IQueueEntry o) {
        if (priority.equals(o.priority)) {
            return offeredNanoTime.compareTo(o.offeredNanoTime)
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

    @Override
    String toString() {
        id
    }


}