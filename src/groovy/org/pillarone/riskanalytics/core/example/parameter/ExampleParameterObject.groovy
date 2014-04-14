package org.pillarone.riskanalytics.core.example.parameter

import org.pillarone.riskanalytics.core.components.InitializingComponent
import org.pillarone.riskanalytics.core.parameterization.AbstractParameterObject
import org.pillarone.riskanalytics.core.parameterization.IParameterObjectClassifier
import org.pillarone.riskanalytics.core.simulation.engine.SimulationScope

class ExampleParameterObject extends AbstractParameterObject implements InitializingComponent {

    Map parameters
    IParameterObjectClassifier classifier

    def injectedScope

    int globalInt
    String globalString

    Map getParameters() {
        return parameters;
    }

    IParameterObjectClassifier getType() {
        return classifier;
    }

    void afterParameterInjection(SimulationScope scope) {
        injectedScope = scope
    }


}
