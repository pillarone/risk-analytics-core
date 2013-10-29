package models.core

import org.pillarone.riskanalytics.core.simulation.engine.ModelTest
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolder
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolderFactory

import static org.junit.Assert.*

/**
 * This does not really test the CoreModel but:
 *
 * - Tests if the ModelTest infrastructure works correctly.
 * - Tests if the parameterization is injected before createPeriodCounter (@see CoreModel)
 * - Tests if the simulation period count has been adjusted to the period counter
 */
class CoreModelTests extends ModelTest {

    Class getModelClass() {
        CoreModel
    }

    @Override
    List<ParameterHolder> getRuntimeParameters() {
        return [
                ParameterHolderFactory.getHolder("runtimeInt", 0, 10) ,
                ParameterHolderFactory.getHolder("runtimeSanityChecks", 0, true) ,
        ]
    }

    void postSimulationEvaluation() {
//        assertEquals 3, run.simulationRun.periodCount

        CoreModel model = runner.currentScope.model
        assertEquals(10, model.exampleInputOutputComponent.runtimeInt)
        assertTrue(model.exampleInputOutputComponent.doCalculationCalled)
        assertTrue(model.exampleInputOutputComponent.afterParameterInjectionCalled)
    }


}
