package org.pillarone.riskanalytics.core.example.migration

import org.pillarone.riskanalytics.core.parameterization.IParameterObject
import org.pillarone.riskanalytics.core.parameterization.IParameterObjectClassifier

/**
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
class EnabledOptionalComponentStrategy implements IParameterObject {

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
