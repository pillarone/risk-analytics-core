package org.pillarone.riskanalytics.core.simulation.engine

import org.pillarone.riskanalytics.core.queue.BasicQueueEntry
import org.pillarone.riskanalytics.core.simulation.engine.grid.SimulationTask

class SimulationQueueEntry extends BasicQueueEntry<SimulationConfiguration> {

    SimulationQueueEntry(SimulationConfiguration simulationConfiguration, int priority) {
        super(new SimulationQueueTaskContext(new SimulationTask(simulationConfiguration: simulationConfiguration), simulationConfiguration), priority)
    }


    SimulationQueueEntry(UUID id) {
        super(id)
    }

    @Override
    SimulationQueueTaskContext getContext() {
        return super.getContext() as SimulationQueueTaskContext
    }
}