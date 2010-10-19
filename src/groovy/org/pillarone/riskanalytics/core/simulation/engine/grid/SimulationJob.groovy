package org.pillarone.riskanalytics.core.simulation.engine.grid

import org.gridgain.grid.GridJobAdapter
import org.pillarone.riskanalytics.core.simulation.engine.SimulationRunner
import org.pillarone.riskanalytics.core.simulation.engine.SimulationConfiguration
import org.gridgain.grid.GridNode
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import java.text.SimpleDateFormat

import org.pillarone.riskanalytics.core.simulation.engine.grid.output.GridOutputStrategy
import org.pillarone.riskanalytics.core.simulation.engine.grid.output.JobResult

class SimulationJob extends GridJobAdapter<JobResult> {

    private SimulationConfiguration simulationConfiguration
    private SimulationRunner runner = SimulationRunner.createRunner()
    private UUID jobIdentifier = UUID.randomUUID()
    private int jobCount = 0;

    private static Log LOG = LogFactory.getLog(SimulationJob)

    public SimulationJob(SimulationConfiguration simulationConfiguration, UUID masterNodeId) {
        this.simulationConfiguration = simulationConfiguration
        this.simulationConfiguration.outputStrategy = new GridOutputStrategy(masterNodeId, runner, jobIdentifier);
    }

    JobResult execute() {
        Date start = new Date()
        ExpandoMetaClass.enableGlobally()
        runner.setJobCount(jobCount)
        runner.setSimulationConfiguration(simulationConfiguration)
        runner.start()

        GridOutputStrategy outputStrategy = this.simulationConfiguration.outputStrategy
        return new JobResult(
                totalMessagesSent: outputStrategy.totalMessages, start: start, end: new Date(),
                nodeName: jobIdentifier.toString(), simulationException: runner.error?.error
        )
    }

    void cancel() {
        runner.cancel()
        super.cancel()
    }

    public void setJobCount(int jobCount) {
        this.jobCount = jobCount;
    }


}
