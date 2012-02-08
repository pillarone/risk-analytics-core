package models.core

import org.pillarone.riskanalytics.core.example.component.ExampleComponentContainingResource
import org.pillarone.riskanalytics.core.example.component.TestComponent2
import org.pillarone.riskanalytics.core.model.StochasticModel

class ResourceModel extends StochasticModel {

    TestComponent2 parameterComponent
    ExampleComponentContainingResource resourceComponent

    void initComponents() {
        parameterComponent = new TestComponent2()
        resourceComponent = new ExampleComponentContainingResource()
        addStartComponent(parameterComponent)
    }

    void wireComponents() {
        resourceComponent.input = parameterComponent.outValue
    }

}
