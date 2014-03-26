package org.pillarone.riskanalytics.core.simulation.engine

import javax.annotation.PostConstruct

/**
 * holds runtime information about running, queued and finished simulations.
 * Will also replace BatchRunInfoService...
 */
class SimulationRuntimeService implements ISimulationQueueListener {

    SimulationQueueService simulationQueueService

    @PostConstruct
    void initialize() {
        simulationQueueService.addSimulationQueueListener(this)
    }

    @Override
    void starting(QueueEntry entry) {
        println("simulation started")
    }

    @Override
    void finished(QueueEntry entry) {
        println("simulation finished")
    }

    @Override
    void offered(QueueEntry entry) {
        println("simulation offered")
    }

    @Override
    void removed(UUID id) {
        println("simulation removed")
    }
}
