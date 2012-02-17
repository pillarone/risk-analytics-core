package org.pillarone.riskanalytics.core.example.component

import org.pillarone.riskanalytics.core.components.AbstractResource
import org.pillarone.riskanalytics.core.simulation.engine.SimulationScope
import org.pillarone.riskanalytics.core.example.parameter.IExampleParameterStrategy
import org.pillarone.riskanalytics.core.example.parameter.ExampleParameterType

class ExampleResource extends AbstractResource {

    boolean defaultCalled = false
    SimulationScope simulationScope

    int parmInteger = 10
    String parmString = "test"
    IExampleParameterStrategy parmStrategy = ExampleParameterType.getDefault()

    void useDefault() {
        defaultCalled = true
    }


}
