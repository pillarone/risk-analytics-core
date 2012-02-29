package org.pillarone.riskanalytics.core.example.parameter

import org.pillarone.riskanalytics.core.parameterization.AbstractParameterObject
import org.pillarone.riskanalytics.core.parameterization.IParameterObjectClassifier

/**
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
class ExampleParameterTypeTwo extends AbstractParameterObject implements IExampleParameterStrategy {

    private double parm1
    private double parm2

    IParameterObjectClassifier getType() {
        ExampleParameterType.TYPE_TWO
    }

    Map getParameters() {
        ['parm1' : parm1, 'parm2' : parm2]
    }
}
