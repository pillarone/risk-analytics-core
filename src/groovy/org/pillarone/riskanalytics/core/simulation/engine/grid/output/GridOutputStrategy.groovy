package org.pillarone.riskanalytics.core.simulation.engine.grid.output

import org.pillarone.riskanalytics.core.output.ICollectorOutputStrategy
import org.pillarone.riskanalytics.core.output.SingleValueResultPOJO
import org.gridgain.grid.GridNode
import org.gridgain.grid.Grid

import org.pillarone.riskanalytics.core.simulation.engine.grid.GridHelper


class GridOutputStrategy implements ICollectorOutputStrategy, Serializable {

    private static final int PACKET_LIMIT = 100000

    private HashMap<ResultDescriptor, ByteArrayOutputStream> streamCache = new HashMap<ResultDescriptor, ByteArrayOutputStream>();

    private Grid grid
    private GridNode node

    private int resultCount = 0

    public GridOutputStrategy(GridNode masterNode) {
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
            ResultDescriptor descriptor = new ResultDescriptor(result.field.id, result.path.id, result.period)
            ByteArrayOutputStream buffer = streamCache.get(descriptor);
            if (buffer == null) {
                buffer = new ByteArrayOutputStream();
                streamCache.put(descriptor, buffer);
            }
            DataOutputStream dos = new DataOutputStream(buffer);
            dos.writeInt(result.iteration);
            dos.writeDouble(result.value);

            resultCount++;

        }
        if (resultCount > PACKET_LIMIT) {
            sendResults()
        }
        return this
    }

    protected void sendResults() {
        for (Map.Entry<ResultDescriptor, ByteArrayOutputStream> entry: streamCache.entrySet()) {
            ResultDescriptor resultDescriptor = entry.key
            ByteArrayOutputStream stream = entry.value
            getGrid().sendMessage(node, new ResultTransferObject(resultDescriptor, stream.toByteArray()));
            stream.reset();
        }

        resultCount = 0
    }
}
