package org.pillarone.riskanalytics.core.simulation.engine.grid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gridgain.grid.*;
import org.joda.time.DateTime;
import org.pillarone.riskanalytics.core.batch.BatchRunInfoService;
import org.pillarone.riskanalytics.core.output.Calculator;
import org.pillarone.riskanalytics.core.output.PathMapping;
import org.pillarone.riskanalytics.core.simulation.SimulationState;
import org.pillarone.riskanalytics.core.simulation.engine.SimulationConfiguration;
import org.pillarone.riskanalytics.core.simulation.engine.grid.mapping.AbstractNodeMappingStrategy;
import org.pillarone.riskanalytics.core.simulation.engine.grid.mapping.INodeMappingStrategy;
import org.pillarone.riskanalytics.core.simulation.engine.grid.output.JobResult;
import org.pillarone.riskanalytics.core.simulation.engine.grid.output.ResultDescriptor;
import org.pillarone.riskanalytics.core.simulation.engine.grid.output.ResultTransferObject;
import org.pillarone.riskanalytics.core.simulation.engine.grid.output.ResultWriter;
import org.pillarone.riskanalytics.core.simulation.item.Simulation;

import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;


public class SimulationTask extends GridTaskAdapter<SimulationConfiguration, Object> {

    private static Log LOG = LogFactory.getLog(SimulationTask.class);

    public static final int SIMULATION_BLOCK_SIZE = 1000;
    public static final int MESSAGE_TIMEOUT = 60000;

    private AtomicInteger messageCount = new AtomicInteger(0);
    private ResultWriter resultWriter;

    private SimulationConfiguration simulationConfiguration;
    private SimulationState currentState = SimulationState.NOT_RUNNING;
    private List<Throwable> simulationErrors = new LinkedList<Throwable>();
    private Map<UUID, Integer> progress = new HashMap<UUID, Integer>();
    private Calculator calculator;

    private long time;
    private int totalJobs = 0;

    private boolean stopped, cancelled;

    private ResultTransferListener resultTransferListener;
    private List<UUID> jobIds = new ArrayList<UUID>();

    public final Map<? extends GridJob, GridNode> map(List<GridNode> subgrid,
                                                      SimulationConfiguration simulationConfiguration)
            throws GridException {
        this.simulationConfiguration = simulationConfiguration;

        INodeMappingStrategy strategy = AbstractNodeMappingStrategy.getStrategy();
        List<GridNode> nodes = new ArrayList<GridNode>(strategy.filterNodes(subgrid));
        Map<SimulationJob, GridNode> jobsToNodes = new HashMap<SimulationJob, GridNode>(nodes.size());
        HashMap<Integer, List<SimulationJob>> jobCountPerGrid = new HashMap<Integer, List<SimulationJob>>();

        setSimulationState(SimulationState.INITIALIZING);
        time = System.currentTimeMillis();
        simulationConfiguration.getSimulation().setStart(new DateTime());

        resultTransferListener = new ResultTransferListener(this);

        Grid grid = GridHelper.getGrid();
        int cpuCount = strategy.getTotalCpuCount(nodes);

        List<SimulationBlock> simulationBlocks = generateBlocks(SIMULATION_BLOCK_SIZE, simulationConfiguration.getSimulation().getNumberOfIterations());

        LOG.info("Number of generated blocks: " + simulationBlocks.size());
        List<SimulationJob> jobs = new ArrayList<SimulationJob>();
        List<SimulationConfiguration> configurations = new ArrayList<SimulationConfiguration>(cpuCount);

        for (int i = 0; i < cpuCount; i++) {
            SimulationConfiguration newConfiguration = simulationConfiguration.clone();
            configurations.add(newConfiguration);
        }

        for (int i = 0; i < simulationBlocks.size(); i++) {
            configurations.get(i % cpuCount).addSimulationBlock(simulationBlocks.get(i));
        }

        for (int i = 0; i < Math.min(cpuCount, simulationBlocks.size()); i++) {
            UUID jobId = UUID.randomUUID();
            SimulationJob job = new SimulationJob(configurations.get(i), jobId, grid.localNode().id());
            jobIds.add(jobId);
            jobs.add(job);
            LOG.info("Created a new job with block count " + configurations.get(i).getSimulationBlocks().size());
        }

        resultWriter = new ResultWriter((Long) simulationConfiguration.getSimulation().id);
        //grid.addMessageListener(this);
        grid.listen(resultTransferListener);

        setSimulationState(SimulationState.RUNNING);
        totalJobs = jobs.size();

        for (int i = 0; i < jobs.size(); i++) {
            int gridNumber = i % nodes.size();
            jobsToNodes.put(jobs.get(i), nodes.get(gridNumber));
            List<SimulationJob> tmpList;
            if ((tmpList = jobCountPerGrid.get(gridNumber)) == null) {
                tmpList = new ArrayList<SimulationJob>();
                jobCountPerGrid.put(gridNumber, tmpList);
            }
            tmpList.add(jobs.get(i));
        }

        for (int i : jobCountPerGrid.keySet()) {
            List<SimulationJob> tmpList = jobCountPerGrid.get(i);
            for (SimulationJob simulationJob : tmpList) {
                simulationJob.setJobCount(tmpList.size());
            }
        }

        return jobsToNodes;
    }

    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
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

        synchronized (this) {
            while (messageCount.get() < totalMessageCount) {
                long timeout = System.currentTimeMillis();
                LOG.info("Not all messages received yet - waiting");
                try {
                    wait(MESSAGE_TIMEOUT);
                } catch (InterruptedException e) {
                    error = true;
                    simulationErrors.add(e);
                    break;
                }
                if (System.currentTimeMillis() - timeout > MESSAGE_TIMEOUT) {
                    error = true;
                    simulationErrors.add(new TimeoutException("Not all messages received - timeout reached"));
                    break;
                }
            }
        }
        resultWriter.close();
        resultTransferListener.removeListener();

        if (error || cancelled) {
            BatchRunInfoService.getService().batchSimulationStateChanged(simulation, currentState);
            simulation.delete();
            setSimulationState(error ? SimulationState.ERROR : SimulationState.CANCELED);
            return false;
        }
        LOG.info("Received " + messageCount + " messages. Sent " + totalMessageCount + " messages.");
        calculator = new Calculator(simulation.getSimulationRun());
        setSimulationState(SimulationState.POST_SIMULATION_CALCULATIONS);
        BatchRunInfoService.getService().batchSimulationStateChanged(simulation, currentState);
        calculator.calculate();

        simulation.setEnd(new DateTime());
        simulation.save();
        setSimulationState(stopped ? SimulationState.STOPPED : SimulationState.FINISHED);
        LOG.info("Task completed in " + (System.currentTimeMillis() - time) + "ms");
        BatchRunInfoService.getService().batchSimulationStateChanged(simulation, currentState);
        return true;
    }

    public synchronized void onMessage(UUID uuid, Object serializable) {
        ResultTransferObject result = (ResultTransferObject) serializable;
        if (!jobIds.contains(result.getJobIdentifier())) {
            return;
        }
        messageCount.incrementAndGet();
        ResultDescriptor rd = result.getResultDescriptor();
        //TODO: should be done before simulation start
        PathMapping pm = simulationConfiguration.getMappingCache().lookupPathDB(rd.getPath());
        rd.setPathId(pm.pathID());
        resultWriter.writeResult(result);
        progress.put(result.getJobIdentifier(), result.getProgress());
        notify();
    }

    public SimulationState getSimulationState() {
        return currentState;
    }

    protected void setSimulationState(SimulationState simulationState) {
        this.currentState = simulationState;
        BatchRunInfoService.getService().batchSimulationStateChanged(getSimulation(), currentState);
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

    public DateTime getEstimatedSimulationEnd() {
        int progress = getProgress();
        if (progress > 0 && currentState == SimulationState.RUNNING) {
            long now = System.currentTimeMillis();
            long onePercentTime = (now - time) / progress;
            long estimatedEnd = now + (onePercentTime * (100 - progress));
            return new DateTime(estimatedEnd);
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

}
