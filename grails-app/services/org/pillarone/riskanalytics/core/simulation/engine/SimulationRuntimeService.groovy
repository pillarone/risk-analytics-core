package org.pillarone.riskanalytics.core.simulation.engine

import org.pillarone.riskanalytics.core.simulation.SimulationState

import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

import static org.pillarone.riskanalytics.core.simulation.SimulationState.CANCELED
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
            List<SimulationRuntimeInfo> infos = new ArrayList<SimulationRuntimeInfo>(queued)
            if (running) {
                infos.add(0, running)
            }
            infos
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
                if (running) {
                    throw new IllegalStateException("starting called, but there is already a running simulation $running.id")
                }
                running = findByQueueId(entry.id)
                if (!running) {
                    throw new IllegalStateException("no info found for id: ${entry.id}")
                }
                queued.remove(running)
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
                if (!running || running.id != id) {
                    throw new IllegalStateException("called finished with id $id. But no running task wikth this id was found.")
                }
                if (timer) {
                    timer.cancel()
                    timer = null
                }
                finished.add(running)
                SimulationState state = running.simulationState
                if (state == FINISHED || state == CANCELED) {
                    fireSimulationInfoEvent(new DeleteSimulationRuntimeInfoEvent(info: running))
                } else {
                    fireSimulationInfoEvent(new ChangeSimulationRuntimeInfoEvent(info: running))
                }
                running = null
            }
        }

        @Override
        void offered(QueueEntry entry) {
            synchronized (lock) {
                SimulationRuntimeInfo info = new SimulationRuntimeInfo(entry)
                queued.add(info)
                queued.sort()
                fireSimulationInfoEvent(new AddSimulationRuntimeInfoEvent(info: info, index: queued.indexOf(info)))
            }
        }

        private SimulationRuntimeInfo findByQueueId(UUID id) {
            queued.find { SimulationRuntimeInfo info -> info.id == id }
        }
    }
}
