package org.pillarone.riskanalytics.core.simulation.engine

import javax.annotation.PostConstruct

/**
 * holds runtime information about running, queued and finished simulations.
 * Will also replace BatchRunInfoService...
 */
class SimulationRuntimeService {
    SimulationQueueService simulationQueueService
    private final List<SimulationRuntimeInfo> finished = []
    private final List<SimulationRuntimeInfo> queued = []
    private SimulationRuntimeInfo running
    private final List<ISimulationRuntimeInfoListener> listeners = []
    private final Object lock = new Object()
    private Timer timer

    @PostConstruct
    void initialize() {
        queued.addAll(simulationQueueService.sortedQueueEntries.collect { QueueEntry entry -> new SimulationRuntimeInfo(entry) })
        simulationQueueService.addSimulationQueueListener(new MyQueueListener())
    }

    void addListener(ISimulationRuntimeInfoListener listener) {
        synchronized (listener) {
            listeners << listener
        }
    }

    void removeListener(ISimulationRuntimeInfoListener listener) {
        synchronized (listener) {
            listeners.remove(listener)
        }
    }


    List<SimulationRuntimeInfo> getQueued() {
        synchronized (lock) {
            new ArrayList<SimulationRuntimeInfo>(queued)
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
                    fireSimulationInfoEvent(new ChangeSimulationRuntimeInfoEvent(info: running))
                }
            }
        }, 2000, 2000);
    }

    void fireSimulationInfoEvent(SimulationRuntimeInfoEvent event) {
        synchronized (listeners) {
            listeners.each { ISimulationRuntimeInfoListener listener -> listener.onEvent(event) }
        }
    }

    private class MyQueueListener implements ISimulationQueueListener {
        @Override
        void starting(QueueEntry entry) {
            synchronized (lock) {
                running = findByQueueEntry(entry)
                fireSimulationInfoEvent(new ChangeSimulationRuntimeInfoEvent(info: running))
                startTimer()
            }
        }

        @Override
        void finished(QueueEntry entry) {
            synchronized (lock) {
                if (timer) {
                    timer.cancel()
                    timer = null
                }
                running == null
                def info = findByQueueEntry(entry)
                if (info) {
                    queued.remove(info)
                    finished.add(info)
                    fireSimulationInfoEvent(new DeleteSimulationRuntimeInfoEvent(info: info))
                }
            }
        }

        @Override
        void offered(QueueEntry entry) {
            synchronized (lock) {
                def index = simulationQueueService.sortedQueueEntries.indexOf(entry)
                if (index != -1) {
                    SimulationRuntimeInfo info = new SimulationRuntimeInfo(entry)
                    queued.add(index, info)
                    fireSimulationInfoEvent(new AddSimulationRuntimeInfoEvent(info: info, index: index))
                }
            }
        }

        private SimulationRuntimeInfo findByQueueEntry(QueueEntry entry) {
            queued.find { SimulationRuntimeInfo info -> info.id == entry.id }
        }
    }
}
