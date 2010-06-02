package org.pillarone.riskanalytics.core.simulation.engine.grid

import org.gridgain.grid.Grid
import org.gridgain.grid.GridNode
import org.pillarone.riskanalytics.core.output.ICollectorOutputStrategy
import org.pillarone.riskanalytics.core.output.SingleValueResultPOJO

/**
 * An output strategy used to send results from the grid node back to the master node.
 */
class GridOutputStrategy implements ICollectorOutputStrategy, Serializable {

    Grid grid
    GridNode node
    ArrayList<Object[]> resultBuffer = new ArrayList<Object[]>(10000)

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
        sendResults()
    }

    ICollectorOutputStrategy leftShift(List results) {
        for (SingleValueResultPOJO result in results) {
            Object[] r = new Object[5]
            r[0] = result.path.id
            r[1] = result.field.id
            r[2] = result.collector.id
            r[3] = result.period
            r[4] = result.value

            resultBuffer << r
        }
        if (resultBuffer.size() > 10000) {
            sendResults()
        }
        return this
    }

    private void sendResults() {
        getGrid().sendMessage(node, resultBuffer)
        resultBuffer.clear()
    }
}
