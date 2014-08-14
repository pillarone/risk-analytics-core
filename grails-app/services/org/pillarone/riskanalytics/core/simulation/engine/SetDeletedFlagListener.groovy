package org.pillarone.riskanalytics.core.simulation.engine

import static org.pillarone.riskanalytics.core.simulation.SimulationState.FINISHED

class SetDeletedFlagListener extends SimulationRuntimeInfoAdapter {
    @Override
    void finished(SimulationRuntimeInfo info) {
        if (info.simulationState != FINISHED) {
            info.deleted = true
        }
    }
}
