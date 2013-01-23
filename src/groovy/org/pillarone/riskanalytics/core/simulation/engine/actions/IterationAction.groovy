package org.pillarone.riskanalytics.core.simulation.engine.actions

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.pillarone.riskanalytics.core.simulation.engine.IterationScope

/**
 * The IterationAction is responsible for iterating over the numberOfPeriods defined fot the simulation.
 * Each period is executed by calling the periodAction.perform() method.
 */

public class IterationAction implements Action {

    private static Log LOG = LogFactory.getLog(IterationAction)

    IterationScope iterationScope
    PeriodAction periodAction
    private boolean stopped = false

    /**
     * Prepares the iterationScope for the next iteration and loops over the defined numberOfPeriods.
     * The periodAction.perform() is called for each period.
     */
    public void perform() {
        if (LOG.isDebugEnabled()) {
            LOG.debug "Start iteration ${iterationScope.currentIteration}"
        }
        iterationScope.prepareNextIteration()
        int numberOfPeriods = iterationScope.numberOfPeriods
        for (int period = 1; period <= numberOfPeriods && !stopped; period++) {
            periodAction.perform()
        }
        periodAction.parameterValidationNeeded = false
        if (LOG.isDebugEnabled()) {
            LOG.debug "End iteration ${iterationScope.currentIteration}"
        }
    }

    /**
     * Stops the iteration at the end of the currentPeriod
     */
    protected void stop() {
        stopped = true
    }

}
