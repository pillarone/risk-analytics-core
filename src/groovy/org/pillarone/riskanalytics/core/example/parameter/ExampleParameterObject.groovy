package org.pillarone.riskanalytics.core.example.parameter

import org.pillarone.riskanalytics.core.parameterization.IParameterObject
import org.pillarone.riskanalytics.core.parameterization.IParameterObjectClassifier

class ExampleParameterObject implements IParameterObject {

    Map parameters
    IParameterObjectClassifier classifier

    Map getParameters() {
        return parameters;
    }

    IParameterObjectClassifier getType() {
        return classifier;
    }


}