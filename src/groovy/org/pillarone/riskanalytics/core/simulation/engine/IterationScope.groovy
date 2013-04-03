package org.pillarone.riskanalytics.core.simulation.engine

import groovy.transform.CompileStatic
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.pillarone.riskanalytics.core.components.PeriodStore
import org.pillarone.riskanalytics.core.simulation.engine.grid.SimulationTask

/**
 * The IterationScope provides information that is valid throughout a single iteration.
 */
@CompileStatic
public class IterationScope {

    private static Log LOG = LogFactory.getLog(IterationScope)

    PeriodScope periodScope

    int currentIteration = 0
    int numberOfPeriods

    List<PeriodStore> periodStores = []

    public void prepareNextIteration() {
        LOG.debug "preparing next iteration"
        currentIteration++
        periodScope.reset()
        for (periodStore in periodStores) {
            periodStore.clear()
        }
    }

    public boolean isFirstIteration() {
        currentIteration % SimulationTask.SIMULATION_BLOCK_SIZE == 1
    }

    public String toString() {
        "current iteration: $currentIteration, $periodScope"
    }
}