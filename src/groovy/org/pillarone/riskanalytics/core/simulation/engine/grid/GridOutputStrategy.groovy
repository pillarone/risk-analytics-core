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
//    ArrayList<Object[]> resultBuffer = new ArrayList<Object[]>(10000)
    StringBuilder buffer = new StringBuilder()
    
    int resCount = 0
    protected long simulationRunId;

    public GridOutputStrategy(GridNode masterNode, long simulationRunId) {
        grid = getGrid()
        node = masterNode
        this.simulationRunId=simulationRunId;
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
            buffer.append simulationRunId+","
            buffer.append result.period+","
            buffer.append result.iteration+","
            buffer.append result.path.id+","
            buffer.append result.field.id+","
            buffer.append result.collector.id+","
            buffer.append result.value+";"
            resCount++
        }
        if (resCount > 10000) {
            sendResults()
        }
        return this
    }

    protected void sendResults() {
        getGrid().sendMessage(node, buffer.toString())
        buffer.delete(0, buffer.length())
        resCount = 0
    }
}