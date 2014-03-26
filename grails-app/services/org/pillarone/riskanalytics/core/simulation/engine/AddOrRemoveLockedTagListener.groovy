package org.pillarone.riskanalytics.core.simulation.engine

import static org.pillarone.riskanalytics.core.simulation.SimulationState.FINISHED

class AddOrRemoveLockedTagListener implements ISimulationRuntimeInfoListener {
    @Override
    void onEvent(SimulationRuntimeInfoEvent event) {
        SimulationRuntimeInfo info = event.info
        if (info.simulationState.equals(FINISHED)) {
            info.simulation.parameterization.addRemoveLockTag()
        }
    }
}
