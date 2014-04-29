package org.pillarone.riskanalytics.core.example.component

import org.pillarone.riskanalytics.core.components.DataSourceDefinition
import org.pillarone.riskanalytics.core.components.InputComponent


class DependingComponent extends InputComponent {

    DataSourceDefinition parmDefinition

    @Override
    DataSourceDefinition getDefinition() {
        return parmDefinition
    }

}
