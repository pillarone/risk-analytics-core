package org.pillarone.riskanalytics.core.simulation.engine

interface QueueListener<T> {

    void starting(T entry)

    void finished(UUID id)

    void removed(UUID id)

    void offered(T entry)

}
