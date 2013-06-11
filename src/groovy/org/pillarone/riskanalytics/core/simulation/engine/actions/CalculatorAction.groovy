package org.pillarone.riskanalytics.core.simulation.engine.actions

import groovy.transform.CompileStatic
import org.pillarone.riskanalytics.core.model.DeterministicModel
import org.pillarone.riskanalytics.core.output.Calculator
import org.pillarone.riskanalytics.core.simulation.SimulationState
import org.pillarone.riskanalytics.core.simulation.engine.SimulationScope

@CompileStatic
public class CalculatorAction implements Action {

    SimulationScope simulationScope
    Calculator calculator

    public void perform() {
        if (!(simulationScope.model instanceof DeterministicModel)) {
            calculator = new Calculator(simulationScope.simulation)
            simulationScope.simulationState = SimulationState.POST_SIMULATION_CALCULATIONS
            calculator.calculate()
        }
    }


}