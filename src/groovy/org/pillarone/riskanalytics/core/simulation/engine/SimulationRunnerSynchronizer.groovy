package org.pillarone.riskanalytics.core.simulation.engine

import java.util.concurrent.CyclicBarrier

class SimulationRunnerSynchronizer implements ISimulationProgressListener {

    private Map<SimulationRunner, Boolean> runnerState = [:]
    private Closure action
    private CyclicBarrier barrier

    SimulationRunnerSynchronizer(List<SimulationRunner> runners, Closure action) {
        runners.each { runnerState.put(it, false); it.progressListeners << this }
        this.action = action
        barrier = new CyclicBarrier(runners.size(), action)
    }

    @Override
    void initializationCompleted(SimulationRunner source) {
        barrier.await()
    }
}
