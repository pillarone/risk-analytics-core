package models.core

import junit.framework.Assert
import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.example.component.ExampleComponentContainingResource
import org.pillarone.riskanalytics.core.simulation.IPeriodCounter
import org.pillarone.riskanalytics.core.simulation.ValuationDatePeriodCounter
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

    /**
     * Used to test if the parameterization is already injected here when running CoreModelTests
     */
    IPeriodCounter createPeriodCounter(DateTime beginOfFirstPeriod) {
        Assert.assertNotNull(exampleInputOutputComponent.parmParameterObject != null)

        //return three dates in order to test if the period count is adjusted correctly
        return new ValuationDatePeriodCounter([new DateTime(), new DateTime(), new DateTime()])
    }

}
