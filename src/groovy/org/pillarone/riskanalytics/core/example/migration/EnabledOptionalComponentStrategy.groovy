package org.pillarone.riskanalytics.core.example.migration

import org.pillarone.riskanalytics.core.parameterization.IParameterObject
import org.pillarone.riskanalytics.core.parameterization.IParameterObjectClassifier
import org.pillarone.riskanalytics.core.parameterization.AbstractParameterObject

/**
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
class EnabledOptionalComponentStrategy extends AbstractParameterObject{

    TimeMode timeMode = TimeMode.PERIOD
    IParameterObject strategy = TestParameterObjectType.getDefault()

    IParameterObjectClassifier getType() {
        OptionalComponentType.ENABLED
    }

    Map getParameters() {
        [timeMode : timeMode,
         strategy : strategy]
    }

}
