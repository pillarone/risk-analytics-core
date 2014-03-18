package org.pillarone.riskanalytics.core.simulation.engine

import grails.transaction.Transactional

@Transactional
class SimulationQueueService {

    PriorityQueue queue = new PriorityQueue()

    def serviceMethod() {
    }

    static class SimulationEntry {

    }
}
