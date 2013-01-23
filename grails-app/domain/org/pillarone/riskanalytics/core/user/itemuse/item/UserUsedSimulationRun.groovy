package org.pillarone.riskanalytics.core.user.itemuse.item

import org.pillarone.riskanalytics.core.user.itemuse.UserUsedItem
import org.pillarone.riskanalytics.core.output.SimulationRun

class UserUsedSimulationRun extends UserUsedItem {

    SimulationRun simulationRun

    static constraints = {
    }

    Class persistedClass() {
      UserUsedSimulationRun
    }
}
