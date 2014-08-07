package org.pillarone.riskanalytics.core.simulation.engine

import org.pillarone.riskanalytics.core.queue.AbstractRuntimeService
import org.pillarone.riskanalytics.core.queue.IQueueService
import org.pillarone.riskanalytics.core.queue.IRuntimeInfoListener

class SimulationRuntimeService extends AbstractRuntimeService<QueueEntry, SimulationRuntimeInfo> {
    SimulationQueueService simulationQueueService

    private final IRuntimeInfoListener addOrRemoveLockedTagListener = new AddOrRemoveLockedTagListener()

    @Override
    void postConstruct() {
        addRuntimeInfoListener(addOrRemoveLockedTagListener)

    }

    @Override
    void preDestroy() {
        removeRuntimeInfoListener(addOrRemoveLockedTagListener)
    }

    @Override
    IQueueService<QueueEntry> getQueueService() {
        return simulationQueueService
    }

    @Override
    SimulationRuntimeInfo createRuntimeInfo(QueueEntry queueEntry) {
        return new SimulationRuntimeInfo(queueEntry)
    }
}
