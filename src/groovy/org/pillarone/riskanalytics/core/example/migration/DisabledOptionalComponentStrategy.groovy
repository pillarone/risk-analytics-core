package org.pillarone.riskanalytics.core.example.migration

import org.pillarone.riskanalytics.core.parameterization.IParameterObjectClassifier
import org.pillarone.riskanalytics.core.parameterization.AbstractParameterObject

/**
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
class DisabledOptionalComponentStrategy extends AbstractParameterObject{

    IParameterObjectClassifier getType() {
        OptionalComponentType.DISABLED
    }

    Map getParameters() {
        [:]
    }

}
