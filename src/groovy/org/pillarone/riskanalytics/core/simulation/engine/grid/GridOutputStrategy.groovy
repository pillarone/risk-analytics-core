package org.pillarone.riskanalytics.core.simulation.engine.grid

import org.pillarone.riskanalytics.core.output.ICollectorOutputStrategy


class GridOutputStrategy implements ICollectorOutputStrategy {

    void finish() {
        
    }

    ICollectorOutputStrategy leftShift(List results) {
        println results.size()
        return this
    }
}
