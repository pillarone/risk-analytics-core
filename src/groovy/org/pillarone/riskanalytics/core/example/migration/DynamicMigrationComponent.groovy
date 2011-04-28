package org.pillarone.riskanalytics.core.example.migration

import org.pillarone.riskanalytics.core.components.DynamicComposedComponent
import org.pillarone.riskanalytics.core.components.Component

/**
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
class DynamicMigrationComponent extends DynamicComposedComponent {

    @Override
    void wire() {
    }

    @Override
    Component createDefaultSubComponent() {
        new OptionalComponent()
    }
}
