package org.pillarone.riskanalytics.core.simulation.engine.actions

import groovy.transform.CompileStatic
import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.simulation.engine.SimulationScope

@CompileStatic
class InjectRuntimeParameterAction implements Action {

    SimulationScope simulationScope

    void perform() {
        Model model = simulationScope.model
        RuntimeParameterCollector parameterCollector = new RuntimeParameterCollector(simulationScope.simulation.runtimeParameters)
        model.accept(parameterCollector)

        parameterCollector.applicableParameters*.apply()
    }


}
