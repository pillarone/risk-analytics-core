package org.pillarone.riskanalytics.core.example.parameter

import org.pillarone.riskanalytics.core.parameterization.AbstractParameterObject
import org.pillarone.riskanalytics.core.parameterization.IParameterObjectClassifier

/**
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
class ExampleParameterTypeOne extends AbstractParameterObject implements IExampleParameterStrategy {

    private double parm1

    IParameterObjectClassifier getType() {
        ExampleParameterType.TYPE_ONE
    }

    Map getParameters() {
        ['parm1' : parm1]
    }
}
