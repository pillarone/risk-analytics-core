package org.pillarone.riskanalytics.core.simulation.engine

interface ISimulationQueueListener {

    void started(QueueEntry entry)

    void finished(QueueEntry entry)

    void offered(QueueEntry entry)

    void removed(UUID id)
}
