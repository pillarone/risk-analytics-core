package org.pillarone.riskanalytics.core.simulation.engine.actions

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.pillarone.riskanalytics.core.simulation.engine.SimulationScope
import org.pillarone.riskanalytics.core.simulation.engine.grid.SimulationBlock

import org.pillarone.riskanalytics.core.util.MathUtils

/**
 * The SimulationAction is responsible for iterating over the number of iterations.
 * Each iteration is executed by calling the IterationActions perform method.
 * The performed iterations get logged to the SimulationScope.iterationsDone property.
 */

public class SimulationAction implements Action {

    private static Log LOG = LogFactory.getLog(SimulationAction)

    IterationAction iterationAction
    SimulationScope simulationScope
    private volatile boolean canceled = false
    private int numberOfIterationsLocal=0;

    /**
     * Loops over the number of iteration and calls iterationAction.perform().
     */
    public void perform() {
        LOG.debug "start perform"
        LOG.info "Using simulation blocks: ${simulationScope.simulationBlocks}"
        for (SimulationBlock simulationBlock: simulationScope.simulationBlocks) {
            initializeSimulationBlock(simulationBlock)
            for (int iteration = 0; iteration < numberOfIterationsLocal && !canceled; iteration++) {
                iterationAction.perform()
                simulationScope.iterationsDone = simulationScope.iterationsDone + 1 // do not use simulationScope.iterationsDone++ because of a issue in StubFor
            }
        }

        LOG.debug "end perform"
    }

    private void initializeSimulationBlock(SimulationBlock simulationBlock) {
        MathUtils.getRandomStreamBase().resetStartStream()
        for (int i = 0; i < simulationBlock.streamOffset; i++) {
            MathUtils.getRandomStreamBase().resetNextSubstream()
        }
        LOG.info "Initialize block: ${simulationBlock}. Reset to substream #${simulationBlock.streamOffset}"
        iterationAction.iterationScope.currentIteration = simulationBlock.iterationOffset
        numberOfIterationsLocal = simulationBlock.blockSize
    }

    void cancel() {
        canceled = true
        iterationAction.stop()
    }

    boolean isCancelled() {
        return canceled
    }

}