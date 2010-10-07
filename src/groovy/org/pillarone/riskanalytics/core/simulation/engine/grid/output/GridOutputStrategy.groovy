package org.pillarone.riskanalytics.core.simulation.engine.grid.output

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.gridgain.grid.Grid
import org.pillarone.riskanalytics.core.output.ICollectorOutputStrategy
import org.pillarone.riskanalytics.core.output.SingleValueResultPOJO
import org.pillarone.riskanalytics.core.simulation.engine.SimulationRunner
import org.pillarone.riskanalytics.core.simulation.engine.grid.GridHelper
import org.gridgain.grid.GridRichNode

class GridOutputStrategy implements ICollectorOutputStrategy, Serializable {

    private static final int PACKET_LIMIT = 100000
    private static Log LOG = LogFactory.getLog(GridOutputStrategy)

    private HashMap<ResultDescriptor, ByteArrayOutputStream> streamCache = new HashMap<ResultDescriptor, ByteArrayOutputStream>();

    private Grid grid
    private UUID masterNodeId
    private SimulationRunner runner
    private UUID jobIdentifier

    private int resultCount = 0

    int totalMessages = 0

    public GridOutputStrategy(UUID masterNodeId, SimulationRunner runner, UUID jobIdentifier) {
        this.masterNodeId = masterNodeId
        this.runner = runner
        this.jobIdentifier = jobIdentifier
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
        LOG.debug("Received ${results.size()} results...")
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
            GridRichNode master
            for(GridRichNode node in getGrid().allNodes) {
                if(node.getId() == this.masterNodeId) {
                    master = node
                }
            }
            getGrid().sendMessage(master, new ResultTransferObject(resultDescriptor, jobIdentifier, stream.toByteArray(), runner.getProgress()));
            totalMessages++
            stream.reset();
        }
        LOG.debug("Sent results back for ${streamCache.size()} streams. Total count: ${totalMessages}")
        resultCount = 0
    }
}
