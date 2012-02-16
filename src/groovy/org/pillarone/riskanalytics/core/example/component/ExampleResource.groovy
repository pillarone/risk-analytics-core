package org.pillarone.riskanalytics.core.example.component

import org.pillarone.riskanalytics.core.components.AbstractResource
import org.pillarone.riskanalytics.core.simulation.engine.SimulationScope

class ExampleResource extends AbstractResource {

    boolean defaultCalled = false
    SimulationScope simulationScope

    int parmInteger = 10
    String parmString = "test"

    void useDefault() {
        defaultCalled = true
    }


}
