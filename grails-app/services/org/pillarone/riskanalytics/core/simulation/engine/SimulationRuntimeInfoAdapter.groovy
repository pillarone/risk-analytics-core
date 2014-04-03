package org.pillarone.riskanalytics.core.simulation.engine

class SimulationRuntimeInfoAdapter implements ISimulationRuntimeInfoListener {
    @Override
    void starting(SimulationRuntimeInfo info) {}

    @Override
    void finished(SimulationRuntimeInfo info) {}

    @Override
    void removed(SimulationRuntimeInfo info) {}

    @Override
    void offered(SimulationRuntimeInfo info) {}

    @Override
    void changed(SimulationRuntimeInfo info) {}
}
