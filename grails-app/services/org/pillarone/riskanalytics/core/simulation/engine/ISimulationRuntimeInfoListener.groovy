package org.pillarone.riskanalytics.core.simulation.engine


interface ISimulationRuntimeInfoListener {
    void starting(SimulationRuntimeInfo info)

    void finished(SimulationRuntimeInfo info)

    void removed(SimulationRuntimeInfo info)

    void offered(SimulationRuntimeInfo info)

    void changed(SimulationRuntimeInfo info)
}

