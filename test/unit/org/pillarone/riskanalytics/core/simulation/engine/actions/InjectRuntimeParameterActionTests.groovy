package org.pillarone.riskanalytics.core.simulation.engine.actions

import models.core.CoreModel
import org.pillarone.riskanalytics.core.simulation.engine.SimulationScope
import org.pillarone.riskanalytics.core.simulation.item.Simulation
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolderFactory

class InjectRuntimeParameterActionTests extends GroovyTestCase {

    void testAction() {
        CoreModel model = new CoreModel()
        model.init()

        assertEquals(1, model.exampleInputOutputComponent.runtimeInt)

        Simulation simulation = new Simulation("Test")
        simulation.addParameter(ParameterHolderFactory.getHolder("runtimeInt", 0, 99))

        InjectRuntimeParameterAction action = new InjectRuntimeParameterAction(simulationScope: new SimulationScope(simulation: simulation, model: model))
        action.perform()

        assertEquals 99, model.exampleInputOutputComponent.runtimeInt
    }
}
