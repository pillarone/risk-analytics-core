package org.pillarone.riskanalytics.core.example.parameter

import org.pillarone.riskanalytics.core.parameterization.AbstractParameterObject
import org.pillarone.riskanalytics.core.parameterization.IParameterObjectClassifier

/**
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
class ExampleParameterTypeTwo extends AbstractParameterObject implements IExampleParameterStrategy {

    private double classifier1
    private double classifier2

    IParameterObjectClassifier getType() {
        ExampleParameterType.TYPE_TWO
    }

    Map getParameters() {
        ['classifier1' : classifier1, 'classifier2' : classifier2]
    }
}
