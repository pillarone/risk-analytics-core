package org.pillarone.riskanalytics.core.simulation.engine

class AddOrRemoveLockedTagListener extends SimulationRuntimeInfoAdapter {
    @Override
    void finished(SimulationRuntimeInfo info) {
        info.simulation.parameterization.addRemoveLockTag()
    }
}
