package org.pillarone.riskanalytics.core.simulation.item.parameter

import org.pillarone.riskanalytics.core.output.OutputStrategy
import org.pillarone.riskanalytics.core.simulation.SimulationState
import org.pillarone.riskanalytics.core.simulation.item.Simulation

class BatchSimulation extends Simulation {

    Integer priority
    OutputStrategy strategy
    SimulationState simulationState

    BatchSimulation(String name) {
        super(name)
    }
}
