package org.pillarone.riskanalytics.core.parameter

import org.pillarone.riskanalytics.core.output.SimulationRun
import org.pillarone.riskanalytics.core.parameter.comment.Tag

class SimulationTag {

    SimulationRun simulationRun

    Tag tag

    static belongsTo = SimulationRun

    String toString() {
        tag
    }

}
