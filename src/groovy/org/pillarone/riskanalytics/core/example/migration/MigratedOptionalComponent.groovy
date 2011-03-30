package org.pillarone.riskanalytics.core.example.migration

import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.parameterization.IParameterObject

/**
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
class MigratedOptionalComponent extends Component {

    IParameterObject parmActive = OptionalComponentType.getStrategy(OptionalComponentType.DISABLED, [:])

    @Override protected void doCalculation() {
    }
}
