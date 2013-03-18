package models.core

import org.pillarone.riskanalytics.core.model.StochasticModel
import org.pillarone.riskanalytics.core.example.component.ExampleOutputComponent
import org.pillarone.riskanalytics.core.example.component.ExampleInputOutputComponent
import org.pillarone.riskanalytics.core.example.component.ExampleDynamicComponent
import org.pillarone.riskanalytics.core.simulation.IPeriodCounter
import org.joda.time.DateTime
import junit.framework.Assert
import org.pillarone.riskanalytics.core.simulation.ValuationDatePeriodCounter
import org.pillarone.riskanalytics.core.example.component.ExampleParameterComponent

/**
 * Model with different components that can be used for tests in the core plugin
 */
class CoreModel extends StochasticModel {

    ExampleOutputComponent exampleOutputComponent
    ExampleOutputComponent hierarchyOutputComponent
    ExampleInputOutputComponent exampleInputOutputComponent
    ExampleDynamicComponent dynamicComponent
    ExampleParameterComponent parameterComponent

    void initComponents() {
        exampleOutputComponent = new ExampleOutputComponent()
        hierarchyOutputComponent = new ExampleOutputComponent()
        exampleInputOutputComponent = new ExampleInputOutputComponent()
        dynamicComponent = new ExampleDynamicComponent()
        parameterComponent = new ExampleParameterComponent()
        addStartComponent(exampleInputOutputComponent)
    }

    void wireComponents() {

    }

    /**
     * Used to test if the parameterization is already injected here when running CoreModelTests
     */
    IPeriodCounter createPeriodCounter(DateTime beginOfFirstPeriod) {
        Assert.assertNotNull(exampleInputOutputComponent.parmParameterObject != null)

        //return three dates in order to test if the period count is adjusted correctly
        return new ValuationDatePeriodCounter([new DateTime(), new DateTime(), new DateTime()])
    }


}