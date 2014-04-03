package org.pillarone.riskanalytics.core.simulation.engine

class SimulationRuntimeInfoEventSupport {
    private final Set<ISimulationRuntimeInfoListener> infoListeners = Collections.synchronizedSet([] as Set)

    void addSimulationRuntimeInfoListener(ISimulationRuntimeInfoListener listener) {
        synchronized (listener) {
            infoListeners << listener
        }
    }

    void removeSimulationRuntimeInfoListener(ISimulationRuntimeInfoListener listener) {
        synchronized (listener) {
            infoListeners.remove(listener)
        }
    }

    void offered(SimulationRuntimeInfo info) {
        synchronized (infoListeners) {
            infoListeners.each { ISimulationRuntimeInfoListener listener -> listener.offered(info) }
        }
    }

    void starting(SimulationRuntimeInfo info) {
        synchronized (infoListeners) {
            infoListeners.each { ISimulationRuntimeInfoListener listener -> listener.starting(info) }
        }
    }

    void finished(SimulationRuntimeInfo info) {
        synchronized (infoListeners) {
            infoListeners.each { ISimulationRuntimeInfoListener listener -> listener.finished(info) }
        }
    }

    void removed(SimulationRuntimeInfo info) {
        synchronized (infoListeners) {
            infoListeners.each { ISimulationRuntimeInfoListener listener -> listener.removed(info) }
        }
    }

    void changed(SimulationRuntimeInfo info) {
        synchronized (infoListeners) {
            infoListeners.each { ISimulationRuntimeInfoListener listener -> listener.changed(info) }
        }
    }
}
