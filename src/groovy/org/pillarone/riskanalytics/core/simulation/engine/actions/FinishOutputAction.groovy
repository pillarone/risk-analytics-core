package org.pillarone.riskanalytics.core.simulation.engine.actions

import org.pillarone.riskanalytics.core.simulation.SimulationState
import org.pillarone.riskanalytics.core.simulation.engine.SimulationScope
import org.pillarone.riskanalytics.core.simulation.engine.actions.Action

public class FinishOutputAction implements Action {

    SimulationScope simulationScope

    public void perform() {
        simulationScope.simulationState = SimulationState.SAVING_RESULTS
        simulationScope.outputStrategy.finish()
    }


}