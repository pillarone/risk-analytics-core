package org.pillarone.riskanalytics.core.simulation.engine.actions

import org.pillarone.riskanalytics.core.simulation.engine.SimulationScope
import org.pillarone.riskanalytics.core.util.MathUtils

class RandomSeedAction implements Action {

    SimulationScope simulationScope

    void perform() {
        if (simulationScope.simulation.randomSeed != null) {
            MathUtils.initRandomStreamBase(simulationScope.simulation.randomSeed)
        }
    }


}