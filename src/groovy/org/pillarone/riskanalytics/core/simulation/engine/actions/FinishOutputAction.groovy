package org.pillarone.riskanalytics.core.simulation.engine.actions

import groovy.transform.CompileStatic
import org.pillarone.riskanalytics.core.simulation.SimulationState
import org.pillarone.riskanalytics.core.simulation.engine.SimulationScope

@CompileStatic
public class FinishOutputAction implements Action {

    SimulationScope simulationScope

    public void perform() {
        simulationScope.simulationState = SimulationState.SAVING_RESULTS
        simulationScope.outputStrategy.finish()
    }


}