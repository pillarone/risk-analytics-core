package org.pillarone.riskanalytics.core.simulation.engine.actions

import org.pillarone.riskanalytics.core.simulation.engine.SimulationScope
import org.pillarone.riskanalytics.core.simulation.IPeriodCounter
import org.pillarone.riskanalytics.core.simulation.ILimitedPeriodCounter

/**
 * A pre-simulation action which creates a period counter from the model and sets it to the period scope.
 * This action should be executed AFTER  {@code PrepareParameterizationAction} because the model may need its
 * parameters injected to create the period counter.
 */
class CreatePeriodCounterAction implements Action {

    SimulationScope simulationScope

    void perform() {
        IPeriodCounter periodCounter = simulationScope.model.createPeriodCounter(simulationScope.simulation.beginOfFirstPeriod)
        simulationScope.iterationScope.periodScope.periodCounter = periodCounter
        if (periodCounter instanceof ILimitedPeriodCounter) {
            simulationScope.simulation.periodCount = periodCounter.periodCount()
            simulationScope.iterationScope.numberOfPeriods = periodCounter.periodCount()
        }
    }
}