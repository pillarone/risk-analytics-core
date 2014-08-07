package org.pillarone.riskanalytics.core.simulation.engine

import org.pillarone.riskanalytics.core.queue.AbstractQueueEntry
import org.pillarone.riskanalytics.core.simulation.engine.grid.SimulationTask
import org.pillarone.riskanalytics.core.user.Person

class SimulationQueueEntry extends AbstractQueueEntry<SimulationQueueTaskContext> {
    int priority
    final Person offeredBy

    SimulationQueueEntry(SimulationConfiguration simulationConfiguration, int priority, Person offeredBy) {
        super(new SimulationQueueTaskContext(new SimulationTask(simulationConfiguration: simulationConfiguration), simulationConfiguration), priority)
        this.offeredBy = offeredBy
    }

    SimulationQueueEntry(UUID id) {
        super(id)
        this.priority = 0
        this.offeredBy = null
    }
}