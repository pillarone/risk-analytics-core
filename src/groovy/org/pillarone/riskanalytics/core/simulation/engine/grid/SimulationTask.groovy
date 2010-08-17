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
    private FileOutputAppender foa = new FileOutputAppender();
    private long simRunId;
//
    protected Collection<? extends GridJob> split(int gridSize, SimulationConfiguration simulationConfiguration) {
        gridSize *= CORE_COUNT
        List<SimulationBlock> simBlocks = generateBlocks(5000, simulationConfiguration.simulation.numberOfIterations);
        //
        int jobSize = simulationConfiguration.simulation.numberOfIterations / gridSize
        simulationConfiguration.simulation.numberOfIterations = jobSize
        def jobs = []

        gridSize.times {
            jobs << new SimulationJob(simulationConfiguration, GridHelper.getGrid().getLocalNode())
        }
        int i=0;
        for (SimulationBlock simBlock:simBlocks){
            ((SimulationJob)jobs.get(i)).addSimulationBlock (simBlock);
            i++;i%=jobs.size();
        }

        gridMysqlBulkInsert.setSimulationRunId simulationConfiguration.simulation.id;
        simRunId = simulationConfiguration.simulation.id;
        foa.init(simRunId);
        GridHelper.getGrid().addMessageListener(this);
//
        return jobs;
    }

    Object reduce(List<GridJobResult> gridJobResults) {
        List l = []
        for (GridJobResult res in gridJobResults) {
            l << res.getData()
        }
        LOG.info "Received ${messageCount} messages"
        //gridMysqlBulkInsert.saveToDB()
        //mdbIndex();

        return l
    }

    void onMessage(UUID uuid, Serializable serializable) {
        messageCount++;
        foa.writeResult serializable
        //gridMysqlBulkInsert.writeResult serializable;
    }

    /*private void mdbIndex(){
        MDBHolder mdbHolder=new MDBHolder("localhost");
        long tstart=System.currentTimeMillis();
        mdbHolder.createIndex ("coll"+simRunId);
        mdbHolder.execQuery ("coll"+simRunId);
        LOG.info ("Creating index took "+(System.currentTimeMillis()-tstart)+"ms");
    }*/

    private List<SimulationBlock> generateBlocks(int blockSize, int iterations) {
        List<SimulationBlock> simBlocks = new ArrayList<SimulationBlock>();
        int iterationOffset = 0;
        int streamOffset = 0;
        for (int i = blockSize; i < iterations; i += blockSize) {
            simBlocks.add(new SimulationBlock(iterationOffset, blockSize, streamOffset++));
            iterationOffset += blockSize;
        }
        simBlocks.add(new SimulationBlock(iterationOffset, iterations - streamOffset * blockSize, streamOffset));
        return simBlocks;
    }
}
