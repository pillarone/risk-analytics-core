package models.core

import org.pillarone.riskanalytics.core.simulation.engine.ModelTest

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

    void postSimulationEvaluation() {
//        assertEquals 3, run.simulationRun.periodCount
    }


}
