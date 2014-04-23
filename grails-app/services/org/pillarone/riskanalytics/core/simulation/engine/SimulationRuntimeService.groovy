package org.pillarone.riskanalytics.core.simulation.engine

import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

/**
 * holds runtime information about running, queued and finished simulations.
 */
class SimulationRuntimeService {
    SimulationQueueService simulationQueueService
    private final List<SimulationRuntimeInfo> finished = []
    private final List<SimulationRuntimeInfo> queued = []
    private SimulationRuntimeInfo running
    private final Object lock = new Object()
    private Timer timer
    private MyQueueListener queueListener
    @Delegate
    private final SimulationRuntimeInfoEventSupport support = new SimulationRuntimeInfoEventSupport()

    @PostConstruct
    void initialize() {
        queueListener = new MyQueueListener()
        simulationQueueService.addSimulationQueueListener(queueListener)
        addSimulationRuntimeInfoListener(new AddOrRemoveLockedTagListener())
    }

    @PreDestroy
    void destroy() {
        simulationQueueService.removeSimulationQueueListener(queueListener)
        queueListener = null
    }

    List<SimulationRuntimeInfo> getQueued() {
        synchronized (lock) {
            List<SimulationRuntimeInfo> infos = new ArrayList<SimulationRuntimeInfo>(queued)
            if (running) {
                infos.add(running)
            }
            infos
        }
    }

    List<SimulationRuntimeInfo> getFinished() {
        synchronized (lock) {
            new ArrayList<SimulationRuntimeInfo>(finished)
        }
    }

    void startTimer() {
        if (timer) {
            timer.cancel()
        }
        timer = new Timer()
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (running != null) {
                    changed(running)
                }
            }
        }, 2000, 2000);
    }

    private class MyQueueListener implements ISimulationQueueListener {
        @Override
        void starting(QueueEntry entry) {
            synchronized (lock) {
                if (running) {
                    throw new IllegalStateException("starting called, but there is already a running simulation $running.id")
                }
                running = findByQueueId(entry.id)
                if (!running) {
                    throw new IllegalStateException("no info found for id: ${entry.id}")
                }
                queued.remove(running)
                starting(running)
                startTimer()
            }
        }

        @Override
        void removed(UUID id) {
            SimulationRuntimeInfo info = findByQueueId(id)
            if (!info) {
                throw new IllegalStateException("no info found for id $id")
            }
            info.simulationTask.cancel()
            queued.remove(info)
            finished.add(info)
            removed(info)
        }

        @Override
        void finished(UUID id) {
            synchronized (lock) {
                if (!running || running.id != id) {
                    throw new IllegalStateException("finished was called, but there is a different task running")
                }
                stopTimer()
                queued.remove(running)
                finished.add(running)
                finished(running)
                running = null
            }
        }

        @Override
        void offered(QueueEntry entry) {
            synchronized (lock) {
                SimulationRuntimeInfo info = new SimulationRuntimeInfo(entry)
                queued.add(info)
                queued.sort()
                offered(info)
            }
        }

        private stopTimer() {
            timer?.cancel()
            timer = null
        }

        private SimulationRuntimeInfo findByQueueId(UUID id) {
            queued.find { SimulationRuntimeInfo info -> info.id == id }
        }
    }
}
