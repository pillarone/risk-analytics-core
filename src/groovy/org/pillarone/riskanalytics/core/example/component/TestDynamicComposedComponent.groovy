package org.pillarone.riskanalytics.core.example.component

import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.components.DynamicComposedComponent
import org.pillarone.riskanalytics.core.example.component.TestComponent

class TestDynamicComposedComponent extends DynamicComposedComponent {

    def inValue
    def outValue

    public Component createDefaultSubComponent() {
        new TestComponent()
    }


    public void wire() {

    }

}