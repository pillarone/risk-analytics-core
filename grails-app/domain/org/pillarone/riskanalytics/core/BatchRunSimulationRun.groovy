package org.pillarone.riskanalytics.core

import org.pillarone.riskanalytics.core.output.SimulationRun
import org.pillarone.riskanalytics.core.output.OutputStrategy

import org.pillarone.riskanalytics.core.simulation.SimulationState

class BatchRunSimulationRun {
    BatchRun batchRun
    SimulationRun simulationRun
    Integer priority
    OutputStrategy strategy
    SimulationState simulationState 

    static constraints = {
        batchRun nullable: false
        simulationRun nullable: false
        strategy nullable: false
    }
}
