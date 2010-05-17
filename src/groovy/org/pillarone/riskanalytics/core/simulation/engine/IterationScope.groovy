package org.pillarone.riskanalytics.core.simulation.engine

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

/**
 * The IterationScope provides infomration that is valid throughout a single iteration.
 */
public class IterationScope {

    private static Log LOG = LogFactory.getLog(IterationScope)

    PeriodScope periodScope

    int currentIteration = 0
    int numberOfPeriods

    List periodStores = []

    public void prepareNextIteration() {
        LOG.debug "preparing next iteration"
        currentIteration++
        periodScope.reset()
        for (periodStore in periodStores) {
            periodStore.clear()
        }
    }

    public String toString() {
        "current iteration: $currentIteration, $periodScope"
    }
}