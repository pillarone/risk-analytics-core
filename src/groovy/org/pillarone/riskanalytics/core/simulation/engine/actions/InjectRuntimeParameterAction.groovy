package org.pillarone.riskanalytics.core.simulation.engine.actions

import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.simulation.engine.SimulationScope

class InjectRuntimeParameterAction implements Action {

    SimulationScope simulationScope

    void perform() {
        Model model = simulationScope.model
        RuntimeParameterCollector parameterCollector = new RuntimeParameterCollector(simulationScope.simulation.runtimeParameters)
        model.accept(parameterCollector)

        parameterCollector.applicableParameters*.apply()
    }


}
