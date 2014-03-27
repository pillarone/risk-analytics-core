package org.pillarone.riskanalytics.core.simulation.engine

import org.pillarone.riskanalytics.core.simulation.SimulationState

import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

import static org.pillarone.riskanalytics.core.simulation.SimulationState.FINISHED

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
    private MyQueueListener queueListener

    @PostConstruct
    void initialize() {
        queued.addAll(simulationQueueService.queueEntries.collect { QueueEntry entry -> new SimulationRuntimeInfo(entry) })
        queueListener = new MyQueueListener()
        simulationQueueService.addSimulationQueueListener(queueListener)
        addListener(new AddOrRemoveLockedTagListener())
    }

    @PreDestroy
    void destroy() {
        simulationQueueService.removeSimulationQueueListener(queueListener)
        queueListener = null
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
                running = findByQueueId(entry.id)
                fireSimulationInfoEvent(new ChangeSimulationRuntimeInfoEvent(info: running))
                startTimer()
            }
        }

        @Override
        void canceled(UUID id) {
            synchronized (lock) {
                def info = findByQueueId(id)
                if (info) {
                    queued.remove(info)
                    finished.add(info)
                    info.simulationTask.cancel()
                    fireSimulationInfoEvent(new DeleteSimulationRuntimeInfoEvent(info: info))
                }
            }
        }

        @Override
        void finished(UUID id) {
            synchronized (lock) {
                if (id == running?.id) {
                    if (timer) {
                        timer.cancel()
                        timer = null
                    }
                    running == null
                }
                def info = findByQueueId(id)
                if (info) {
                    queued.remove(info)
                    finished.add(info)
                    SimulationState state = info.simulationState
                    if (state == FINISHED) {
                        fireSimulationInfoEvent(new DeleteSimulationRuntimeInfoEvent(info: info))
                    } else {
                        fireSimulationInfoEvent(new ChangeSimulationRuntimeInfoEvent(info: info))
                    }
                }
            }
        }

        @Override
        void offered(QueueEntry entry) {
            synchronized (lock) {
                def index = simulationQueueService.queueEntries.indexOf(entry)
                if (index != -1) {
                    SimulationRuntimeInfo info = new SimulationRuntimeInfo(entry)
                    queued.add(index, info)
                    fireSimulationInfoEvent(new AddSimulationRuntimeInfoEvent(info: info, index: index))
                }
            }
        }

        private SimulationRuntimeInfo findByQueueId(UUID id) {
            queued.find { SimulationRuntimeInfo info -> info.id == id }
        }
    }
}
