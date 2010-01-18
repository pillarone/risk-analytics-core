package org.pillarone.riskanalytics.core.example.component

import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.components.DynamicComposedComponent

class ExampleDynamicComponent extends DynamicComposedComponent {

    void wire() {

    }

    Component createDefaultSubComponent() {
        return new ExampleInputOutputComponent()
    }


}