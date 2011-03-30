package org.pillarone.riskanalytics.core.example.migration

import org.pillarone.riskanalytics.core.parameterization.IParameterObject
import org.pillarone.riskanalytics.core.parameterization.IParameterObjectClassifier

/**
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
class DisabledOptionalComponentStrategy implements IParameterObject {

    IParameterObjectClassifier getType() {
        OptionalComponentType.DISABLED
    }

    Map getParameters() {
        [:]
    }

}
