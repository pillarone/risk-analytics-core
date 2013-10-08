package org.pillarone.riskanalytics.core.simulation.engine

import java.util.concurrent.CountDownLatch
import java.util.concurrent.CyclicBarrier


class SimulationRunnerSynchronizer implements ISimulationProgressListener {

    private Map<SimulationRunner, Boolean> runnerState = [:]
    private Closure action
//    private CountDownLatch countDownLatch
    private CyclicBarrier barrier

    SimulationRunnerSynchronizer(List<SimulationRunner> runners, Closure action) {
        runners.each { runnerState.put(it, false); it.progressListeners << this }
        this.action = action
//        countDownLatch = new CountDownLatch(runners.size())
        barrier = new CyclicBarrier(runners.size(), action)
    }

    @Override
    void initializationCompleted(SimulationRunner source) {
        barrier.await()
//        println("Latch count = ${countDownLatch.count}")
//        if (countDownLatch.count == 1) {
//            action.call()
//            countDownLatch.countDown()
//        } else {
//            countDownLatch.countDown()
//            countDownLatch.await()
//        }


//        println("called ${Thread.currentThread().name}: ${runnerState}")
//        runnerState[source] = true
//        if (runnerState.values().every { it }) {
//            println("finished ${Thread.currentThread().name}: ${runnerState}")
//            action.call()
//            for (SimulationRunner runner in runnerState.keySet()) {
//
//            }
//        } else {
//
//        }
    }
}
