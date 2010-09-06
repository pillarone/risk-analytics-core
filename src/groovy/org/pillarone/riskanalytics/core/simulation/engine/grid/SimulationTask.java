package org.pillarone.riskanalytics.core.simulation.engine.grid;

import org.gridgain.grid.GridTaskSplitAdapter;
import org.pillarone.riskanalytics.core.output.Calculator;
import org.pillarone.riskanalytics.core.simulation.SimulationState;
import org.pillarone.riskanalytics.core.simulation.engine.SimulationConfiguration;
import org.gridgain.grid.GridJob;
import org.gridgain.grid.GridJobResult;

import org.gridgain.grid.GridMessageListener;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.gridgain.grid.Grid;
import org.gridgain.grid.GridNode;
import org.pillarone.riskanalytics.core.simulation.engine.grid.output.JobResult;
import org.pillarone.riskanalytics.core.simulation.engine.grid.output.ResultWriter;
import org.pillarone.riskanalytics.core.simulation.engine.grid.output.ResultTransferObject;
import org.pillarone.riskanalytics.core.simulation.item.Simulation;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.TimeoutException;


public class SimulationTask extends GridTaskSplitAdapter<SimulationConfiguration, Object> implements GridMessageListener {

    private static Log LOG = LogFactory.getLog(SimulationTask.class);

    public static final int SIMULATION_BLOCK_SIZE = 1000;
    public static final int MESSAGE_TIMEOUT=60;

    private int messageCount = 0;
    private ResultWriter resultWriter;

    private SimulationConfiguration simulationConfiguration;
    private SimulationState currentState = SimulationState.NOT_RUNNING;
    private List<Throwable> simulationErrors = new LinkedList<Throwable>();
    private Map<UUID, Integer> progress = new HashMap<UUID, Integer>();
    private Calculator calculator;

    private long time,start;
    private int totalJobs = 0;

    private boolean stopped, cancelled;

    protected Collection<? extends GridJob> split(int gridSize, SimulationConfiguration simulationConfiguration) {
        currentState = SimulationState.INITIALIZING;
        time = System.currentTimeMillis();
        simulationConfiguration.getSimulation().setStart(new Date());

        Grid grid = GridHelper.getGrid();
        int cpuCount = getTotalProcessorCount(grid);

        List<SimulationBlock> simulationBlocks = generateBlocks(SIMULATION_BLOCK_SIZE, simulationConfiguration.getSimulation().getNumberOfIterations());

        LOG.info("Number of generated blocks: " + simulationBlocks.size());
        int maximumBlocksPerNode = new BigDecimal(simulationBlocks.size()).divide(new BigDecimal(cpuCount), RoundingMode.UP).intValue();
        List<SimulationJob> jobs = new ArrayList<SimulationJob>();

        int nextBlockIndex = 0;
        for (int i = 0; i < cpuCount; i++) {
            SimulationConfiguration newConfiguration = simulationConfiguration.clone();
            for (int j = 0; j < maximumBlocksPerNode; j++) {
                if (nextBlockIndex < simulationBlocks.size()) {
                    newConfiguration.addSimulationBlock(simulationBlocks.get(nextBlockIndex));
                    nextBlockIndex++;
                }
            }
            if (newConfiguration.getSimulationBlocks().size() > 0) {
                jobs.add(new SimulationJob(newConfiguration, grid.getLocalNode()));
                LOG.info("Created a new job with block count " + newConfiguration.getSimulationBlocks().size());
            }
        }

        resultWriter = new ResultWriter((Long) simulationConfiguration.getSimulation().id);
        grid.addMessageListener(this);

        this.simulationConfiguration = simulationConfiguration;
        currentState = SimulationState.RUNNING;
        totalJobs = jobs.size();
        start=System.currentTimeMillis();
        return jobs;
    }

    public Object reduce(List<GridJobResult> gridJobResults) {
        int totalMessageCount = 0;
        boolean error = false;
        for (GridJobResult res : gridJobResults) {
            JobResult jobResult = res.getData();
            totalMessageCount += jobResult.getTotalMessagesSent();

            LOG.info("Job " + jobResult.getNodeName() + " executed in " + (jobResult.getEnd().getTime() - jobResult.getStart().getTime()) + " ms");
            Throwable simulationException = jobResult.getSimulationException();
            if (simulationException != null) {
                LOG.error("Error in job " + jobResult.getNodeName(), simulationException);
                simulationErrors.add(simulationException);
                error = true;
            }
        }
        Simulation simulation = simulationConfiguration.getSimulation();

        int currentMessageCount=messageCount;
        long timeout=System.currentTimeMillis()+MESSAGE_TIMEOUT*1000;
        while (messageCount<totalMessageCount){
            if (messageCount>currentMessageCount){
                timeout=System.currentTimeMillis()+MESSAGE_TIMEOUT*1000;
                currentMessageCount=messageCount;
            }
            if (System.currentTimeMillis()>timeout){
                LOG.error ("Not all messages received ... timeout reached");
                error=true;
                simulationErrors.add(new TimeoutException("Not all messages received before timeout ("+messageCount+
                " of total "+totalMessageCount+")"));
                break;
            }
            try{
                Thread.sleep(1);
            }catch(Exception e){};
        }

        Grid grid = GridHelper.getGrid();
        grid.removeMessageListener(this);
        if (error || cancelled) {
            simulation.delete();
            currentState = error ? SimulationState.ERROR : SimulationState.CANCELED;
            GridHelper.getGrid().removeMessageListener(this);
            return false;
        }
        LOG.info("Received " + messageCount + " messages. Sent " + totalMessageCount + " messages.");
        calculator = new Calculator(simulation.getSimulationRun());
        currentState = SimulationState.POST_SIMULATION_CALCULATIONS;
        calculator.calculate();

        simulation.setEnd(new Date());
        simulation.save();
        currentState = stopped ? SimulationState.STOPPED : SimulationState.FINISHED;
        LOG.info("Task completed in " + (System.currentTimeMillis() - time) + "ms");
        GridHelper.getGrid().removeMessageListener(this);
        return true;
    }

    public synchronized void onMessage(UUID uuid, Serializable serializable) {
        messageCount++;
        ResultTransferObject result = (ResultTransferObject) serializable;
        resultWriter.writeResult(result);
        progress.put(result.getJobIdentifier(), result.getProgress());
    }

    public SimulationState getSimulationState() {
        return currentState;
    }

    public Simulation getSimulation() {
        return simulationConfiguration.getSimulation();
    }

    public List<Throwable> getSimulationErrors() {
        return simulationErrors;
    }

    public void cancel() {
        cancelled = true;
    }

    public void stop() {
        stopped = true;
    }

    public int getProgress() {
        if (!(currentState == SimulationState.POST_SIMULATION_CALCULATIONS)) {
            if (progress.isEmpty()) {
                return 0;
            }
            int sum = 0;
            for (Integer value : progress.values()) {
                sum += value;
            }
            return sum / totalJobs;
        } else {
            return calculator.getProgress();
        }
    }

    public Date getEstimatedSimulationEnd() {
        int progress = getProgress();
        if (progress > 0 && currentState == SimulationState.RUNNING) {
            long now = System.currentTimeMillis();
            long onePercentTime = (now - start) / progress;
            long estimatedEnd = now + (onePercentTime * (100 - progress));
            return new Date(estimatedEnd);
        } else if (currentState == SimulationState.POST_SIMULATION_CALCULATIONS) {
            return calculator.getEstimatedEnd();
        }
        return null;
    }

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

    protected int getTotalProcessorCount(Grid grid) {
        Collection<GridNode> nodes = grid.getAllNodes();
        List<String> usedHosts = new ArrayList<String>();
        int processorCount = 0;
        for (GridNode node : nodes) {
            if (!usedHosts.contains(node.getPhysicalAddress())) {
                processorCount += node.getMetrics().getAvailableProcessors();
                usedHosts.add(node.getPhysicalAddress());
            }
        }
        LOG.info("Found " + processorCount + " CPUs on " + nodes.size() + " nodes");
        return processorCount;
    }
}
