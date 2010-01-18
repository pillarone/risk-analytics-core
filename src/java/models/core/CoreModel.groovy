package models.core

import org.pillarone.riskanalytics.core.model.StochasticModel
import org.pillarone.riskanalytics.core.example.component.ExampleOutputComponent
import org.pillarone.riskanalytics.core.example.component.ExampleInputOutputComponent
import org.pillarone.riskanalytics.core.example.component.ExampleDynamicComponent

/**
 * Model with different components that can be used for tests in the core plugin
 */
class CoreModel extends StochasticModel {

    ExampleOutputComponent exampleOutputComponent
    ExampleOutputComponent hierarchyOutputComponent
    ExampleInputOutputComponent exampleInputOutputComponent
    ExampleDynamicComponent dynamicComponent

    void initComponents() {
        exampleOutputComponent = new ExampleOutputComponent()
        hierarchyOutputComponent = new ExampleOutputComponent()
        exampleInputOutputComponent = new ExampleInputOutputComponent()
        dynamicComponent = new ExampleDynamicComponent()
    }

    void wireComponents() {

    }


}