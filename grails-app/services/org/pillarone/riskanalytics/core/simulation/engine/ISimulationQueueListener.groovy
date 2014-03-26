package org.pillarone.riskanalytics.core.simulation.engine

interface ISimulationQueueListener {

    void starting(QueueEntry entry)

    void finished(QueueEntry entry)

    void offered(QueueEntry entry)
}
