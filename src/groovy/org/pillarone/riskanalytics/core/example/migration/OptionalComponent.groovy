package org.pillarone.riskanalytics.core.example.migration

import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.parameterization.IParameterObject

/**
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
class OptionalComponent extends Component {

    TimeMode parmTimeMode = TimeMode.PERIOD
    IParameterObject parmStrategy = TestParameterObjectType.getDefault()

    @Override protected void doCalculation() {
    }
}
