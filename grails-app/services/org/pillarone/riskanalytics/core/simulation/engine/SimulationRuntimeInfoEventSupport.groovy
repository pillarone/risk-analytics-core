package org.pillarone.riskanalytics.core.simulation.engine

class SimulationRuntimeInfoEventSupport {
    private final List<ISimulationRuntimeInfoListener> infoListeners = []

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

    void fireSimulationInfoEvent(SimulationRuntimeInfoEvent event) {
        synchronized (infoListeners) {
            infoListeners.each { ISimulationRuntimeInfoListener listener -> listener.onEvent(event) }
        }
    }
}
