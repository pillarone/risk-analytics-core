package org.pillarone.riskanalytics.core.simulation.engine.actions

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.pillarone.riskanalytics.core.simulation.engine.SimulationScope

/**
 * The SimulationAction is responsible for iterating over the number of iterations.
 * Each iteration is executed by calling the IterationActions perform method.
 * The performed iterations get logged to the SimulationScope.iterationsDone property.
 */

public class SimulationAction implements Action {

    private static Log LOG = LogFactory.getLog(SimulationAction)

    IterationAction iterationAction
    SimulationScope simulationScope
    private volatile boolean stopped = false



    /**
     * Loops over the number of iteration and calls iterationAction.perform().
     */
    public void perform() {
        LOG.debug "start perform"
        int numberOfIterations = simulationScope.numberOfIterations
        for (int iteration = 0; iteration < numberOfIterations && !stopped; iteration++) {
            iterationAction.perform()
            simulationScope.iterationsDone = simulationScope.iterationsDone + 1 // do not use simulationScope.iterationsDone++ because of a issue in StubFor
        }

        LOG.debug "end perform"
    }

    /**
     * Stops the simulation at the end of the current point of execution. The stop is forwarded to the iterationAction.
     */
    protected void stop() {
        stopped = true
        iterationAction.stop()
    }
}