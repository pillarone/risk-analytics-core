package org.pillarone.riskanalytics.core.simulation.engine.grid

import org.pillarone.riskanalytics.core.output.ICollectorOutputStrategy
import org.gridgain.grid.Grid
import org.gridgain.grid.GridNode
import org.pillarone.riskanalytics.core.simulation.item.parameter.DateParameterHolder
import org.joda.time.DateTime

/**
 * An output strategy used to send results from the grid node back to the master node. 
 */
class GridOutputStrategy implements ICollectorOutputStrategy, Serializable {

    Grid grid
    GridNode node

    public GridOutputStrategy(GridNode masterNode) {
        grid = getGrid()
        node = masterNode
    }

    private Grid getGrid() {
        if (grid == null) {
            grid = GridHelper.getGrid()
        }
        return grid
    }

    void finish() {

    }

    ICollectorOutputStrategy leftShift(List results) {
//            getGrid().sendMessage(node, results)
        return this
    }
}
