package org.pillarone.riskanalytics.core.simulation.engine

import org.pillarone.riskanalytics.core.queue.AbstractRuntimeService
import org.pillarone.riskanalytics.core.queue.IQueueService
import org.pillarone.riskanalytics.core.queue.IRuntimeInfoListener

class SimulationRuntimeService extends AbstractRuntimeService<SimulationQueueEntry, SimulationRuntimeInfo> {
    SimulationQueueService simulationQueueService

    private final IRuntimeInfoListener addOrRemoveLockedTagListener = new AddOrRemoveLockedTagListener()
    private final IRuntimeInfoListener setDeletedFlagListener = new SetDeletedFlagListener()

    @Override
    void postConstruct() {
        addRuntimeInfoListener(addOrRemoveLockedTagListener)
        addRuntimeInfoListener(setDeletedFlagListener)
    }

    @Override
    void preDestroy() {
        removeRuntimeInfoListener(addOrRemoveLockedTagListener)
        removeRuntimeInfoListener(setDeletedFlagListener)
    }

    @Override
    IQueueService<SimulationQueueEntry> getQueueService() {
        return simulationQueueService
    }

    @Override
    SimulationRuntimeInfo createRuntimeInfo(SimulationQueueEntry queueEntry) {
        return new SimulationRuntimeInfo(queueEntry)
    }
}
