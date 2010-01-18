package org.pillarone.riskanalytics.core.example.model

import org.pillarone.riskanalytics.core.example.component.ExampleInputOutputComponent
import org.pillarone.riskanalytics.core.example.component.TestComponentWithPeriodStore
import org.pillarone.riskanalytics.core.example.component.TestComponentWithSimulationContext
import org.pillarone.riskanalytics.core.example.component.TestComponentWithSimulationContextAndPeriodStore
import org.pillarone.riskanalytics.core.example.component.TestComposedComponentCtxPeriodStore
import org.pillarone.riskanalytics.core.model.StochasticModel

class TestModel extends StochasticModel {

    ExampleInputOutputComponent frequencyGenerator
    ExampleInputOutputComponent claimsGenerator

    public void initComponents() {
        frequencyGenerator = new ExampleInputOutputComponent()
        claimsGenerator = new ExampleInputOutputComponent()
        addStartComponent frequencyGenerator
    }

    public void wireComponents() {
        claimsGenerator.inValue = frequencyGenerator.outValue
    }
}
class TestModelForInjection extends StochasticModel {
    TestComponentWithPeriodStore componentPS
    TestComponentWithSimulationContext componentCtx
    TestComponentWithSimulationContextAndPeriodStore componentCtxPS
    TestComposedComponentCtxPeriodStore composedComponent

    public void initComponents() {
        componentPS = new TestComponentWithPeriodStore()
        componentCtx = new TestComponentWithSimulationContext()
        componentCtxPS = new TestComponentWithSimulationContextAndPeriodStore()
        composedComponent = new TestComposedComponentCtxPeriodStore()

        addStartComponent componentPS
    }

    public void wireComponents() {
        componentCtx.input = componentPS.outPacket
        componentCtxPS.input = componentCtx.output
        composedComponent.input = componentCtxPS.output
    }
}