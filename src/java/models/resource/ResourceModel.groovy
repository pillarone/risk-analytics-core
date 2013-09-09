package models.resource

import org.pillarone.riskanalytics.core.example.component.ExampleComponentContainingResource
import org.pillarone.riskanalytics.core.example.component.TestComponent2
import org.pillarone.riskanalytics.core.model.StochasticModel
import org.pillarone.riskanalytics.core.example.component.ExampleParameterComponent

class ResourceModel extends StochasticModel {

    TestComponent2 parameterComponent
    ExampleComponentContainingResource resourceComponent
    ExampleParameterComponent globalParameterComponent


    void initComponents() {
        parameterComponent = new TestComponent2()
        resourceComponent = new ExampleComponentContainingResource()
        globalParameterComponent = new ExampleParameterComponent()
        addStartComponent(parameterComponent)
    }

    void wireComponents() {
        resourceComponent.input = parameterComponent.outValue
    }

}
