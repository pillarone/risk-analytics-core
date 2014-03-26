package org.pillarone.riskanalytics.core.simulation.engine

class AddOrRemoveLockedTagListener implements ISimulationQueueListener {
    @Override
    void starting(QueueEntry entry) {}

    @Override
    void finished(QueueEntry entry) {
        entry.simulationConfiguration.simulation.parameterization.addRemoveLockTag()
    }

    @Override
    void offered(QueueEntry entry) {}
}
