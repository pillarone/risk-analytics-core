package org.pillarone.riskanalytics.core.simulation.engine.grid

import org.gridgain.grid.GridTaskSplitAdapter
import org.pillarone.riskanalytics.core.simulation.engine.SimulationConfiguration
import org.gridgain.grid.GridJob
import org.gridgain.grid.GridJobResult
import org.gridgain.grid.GridFactory
import org.gridgain.grid.GridMessageListener
import org.apache.commons.logging.LogFactory
import org.apache.commons.logging.Log
import org.pillarone.riskanalytics.core.output.batch.results.GridMysqlBulkInsert


class SimulationTask extends GridTaskSplitAdapter<SimulationConfiguration, Object> implements GridMessageListener {

    private int messageCount = 0;
    protected static final int CORE_COUNT = 2
    private static Log LOG = LogFactory.getLog(SimulationTask)
    private GridMysqlBulkInsert gridMysqlBulkInsert = new GridMysqlBulkInsert();

    protected Collection<? extends GridJob> split(int gridSize, SimulationConfiguration simulationConfiguration) {
        gridSize *= CORE_COUNT
        int jobSize = simulationConfiguration.simulation.numberOfIterations / gridSize
        simulationConfiguration.simulation.numberOfIterations = jobSize
        def jobs = []
        gridSize.times {
            jobs << new SimulationJob(simulationConfiguration, GridHelper.getGrid().getLocalNode())
        }
        gridMysqlBulkInsert.setSimulationRunId simulationConfiguration.simulation.id;
        GridHelper.getGrid().addMessageListener(this);
        return jobs;
    }

    Object reduce(List<GridJobResult> gridJobResults) {
        List l = []
        for (GridJobResult res in gridJobResults) {
            l << res.getData()
        }
        LOG.info "Received ${messageCount} messages"
        gridMysqlBulkInsert.saveToDB()
        return l
    }

    void onMessage(UUID uuid, Serializable serializable) {
        messageCount++;
        gridMysqlBulkInsert.writeResult serializable;
    }
}
