package org.pillarone.riskanalytics.core.simulation.engine

import org.pillarone.riskanalytics.core.queue.IRuntimeInfoListener

class SimulationRuntimeInfoAdapter implements IRuntimeInfoListener<SimulationRuntimeInfo> {
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
