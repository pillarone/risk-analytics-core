package org.pillarone.riskanalytics.core.simulation.engine

interface ISimulationQueueListener {

    void starting(QueueEntry entry)

    void finished(UUID id)

    void canceled(UUID id)

    void offered(QueueEntry entry)
}
