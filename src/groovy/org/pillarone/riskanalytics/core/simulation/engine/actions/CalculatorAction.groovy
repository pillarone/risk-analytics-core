package org.pillarone.riskanalytics.core.simulation.engine.actions

import org.pillarone.riskanalytics.core.output.Calculator
import org.pillarone.riskanalytics.core.simulation.SimulationState
import org.pillarone.riskanalytics.core.simulation.engine.SimulationScope

public class CalculatorAction implements Action {

    SimulationScope simulationScope
    Calculator calculator

    public void perform() {
        calculator = new Calculator(simulationScope.simulationRun)
        simulationScope.simulationState = SimulationState.POST_SIMULATION_CALCULATIONS
        calculator.calculate()
    }


}