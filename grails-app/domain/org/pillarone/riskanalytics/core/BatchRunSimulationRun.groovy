package org.pillarone.riskanalytics.core

import org.hibernate.FetchMode
import org.pillarone.riskanalytics.core.output.OutputStrategy
import org.pillarone.riskanalytics.core.output.SimulationRun
import org.pillarone.riskanalytics.core.simulation.SimulationState

class BatchRunSimulationRun {
    BatchRun batchRun
    SimulationRun simulationRun
    Integer priority
    OutputStrategy strategy
    volatile SimulationState simulationState

    static mapping = {
        batchRun lazy: false, fetchMode: FetchMode.JOIN, cache: 'read-only'
        simulationRun lazy: false, fetchMode: FetchMode.JOIN, cache: 'read-only'
    }

    static constraints = {
        batchRun nullable: false
        simulationRun nullable: false
        strategy nullable: false
    }
}
