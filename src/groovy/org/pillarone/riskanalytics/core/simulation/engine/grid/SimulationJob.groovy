package org.pillarone.riskanalytics.core.simulation.engine.grid

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.gridgain.grid.GridJobAdapter
import org.pillarone.riskanalytics.core.output.aggregation.PacketAggregatorRegistry
import org.pillarone.riskanalytics.core.simulation.engine.SimulationConfiguration
import org.pillarone.riskanalytics.core.simulation.engine.SimulationRunner
import org.pillarone.riskanalytics.core.simulation.engine.grid.output.GridOutputStrategy
import org.pillarone.riskanalytics.core.simulation.engine.grid.output.JobResult
import org.pillarone.riskanalytics.core.output.aggregation.IPacketAggregator
import org.pillarone.riskanalytics.core.simulation.IPeriodCounter
import org.pillarone.riskanalytics.core.simulation.ILimitedPeriodCounter
import org.pillarone.riskanalytics.core.components.ResourceRegistry
import org.pillarone.riskanalytics.core.simulation.item.Resource

class SimulationJob extends GridJobAdapter<JobResult> {

    private SimulationConfiguration simulationConfiguration
    private SimulationRunner runner = SimulationRunner.createRunner()
    private UUID jobIdentifier
    private int jobCount = 0;

    private static Log LOG = LogFactory.getLog(SimulationJob)

    Map<Class, IPacketAggregator> aggregatorMap = [:]
    List<Resource> loadedResources = []

    public SimulationJob(SimulationConfiguration simulationConfiguration, UUID jobId, UUID masterNodeId) {
        this.jobIdentifier = jobId
        this.simulationConfiguration = simulationConfiguration
        this.simulationConfiguration.outputStrategy = new GridOutputStrategy(masterNodeId, runner, jobIdentifier);
    }

    JobResult execute() {

        //***** https://issuetracking.intuitive-collaboration.com/jira/browse/KTI-15
        getClass().getClassLoader().loadClass("java.lang.Character")
        getClass().getClassLoader().loadClass("java.lang.reflect.InvocationTargetException")
        getClass().getClassLoader().loadClass("org.pillarone.riskanalytics.core.components.ComponentUtils")
        //***** http://www.gridgainsystems.com/jiveforums/thread.jspa?threadID=1324&tstart=0

        for(Map.Entry<Class, IPacketAggregator> entry in aggregatorMap) {
            PacketAggregatorRegistry.registerAggregator(entry.key, entry.value)
        }
        ResourceRegistry.preLoad(loadedResources)

        Date start = new Date()
        ExpandoMetaClass.enableGlobally()
        runner.setJobCount(jobCount)
        runner.setSimulationConfiguration(simulationConfiguration)
        runner.start()

        GridOutputStrategy outputStrategy = this.simulationConfiguration.outputStrategy
        final JobResult result = new JobResult(
                totalMessagesSent: outputStrategy.totalMessages, start: start, end: new Date(),
                nodeName: jobIdentifier.toString(), simulationException: runner.error?.error
        )
        final IPeriodCounter periodCounter = runner.currentScope.iterationScope.periodScope.periodCounter
        if(periodCounter instanceof ILimitedPeriodCounter) {
            result.numberOfSimulatedPeriods = periodCounter.periodCount()
        }
        return result
    }

    void cancel() {
        runner.cancel()
        super.cancel()
    }

    public void setJobCount(int jobCount) {
        this.jobCount = jobCount;
    }


    UUID getJobIdentifier() {
        return jobIdentifier
    }

}
