package org.pillarone.riskanalytics.core.batch

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.pillarone.riskanalytics.core.BatchRunSimulationRun
import org.pillarone.riskanalytics.core.output.SimulationRun
import org.pillarone.riskanalytics.core.simulation.SimulationState
import org.pillarone.riskanalytics.core.simulation.item.Simulation

/**
 * @author fouad.jaada@intuitive-collaboration.com
 */
class BatchRunInfoService {

    List<SimulationRun> activeSimulationRuns
    List<Simulation> executedSimulations
    List<BatchRunSimulationRun> executedBatchRunSimulationRuns
    List<BatchRunSimulationRun> updatedRuns
    List runningSimulations = []

    Log LOG = LogFactory.getLog(BatchRunInfoService)

    public BatchRunInfoService() {
        activeSimulationRuns = []
        executedSimulations = []
        executedBatchRunSimulationRuns = []
    }

    public static BatchRunInfoService getService() {
        return ApplicationHolder.getApplication().getMainContext().getBean('batchRunInfoService')
    }

    public synchronized void batchSimulationRunEnd(Simulation simulation, SimulationState simulationState) {
        executedSimulations << simulation
        activeSimulationRuns.remove(activeSimulationRuns.find { it.name == simulation.name})
        BatchRunSimulationRun batchRunSimulationRun = update(simulation, simulationState)
        addExecutedBatch(batchRunSimulationRun)
    }

    public synchronized void batchSimulationStateChanged(Simulation simulation, SimulationState simulationState) {
        if (simulationState == SimulationState.FINISHED || simulationState == SimulationState.ERROR) {
            batchSimulationRunEnd(simulation, simulationState)
        } else {
            BatchRunSimulationRun batchRunSimulationRun = update(simulation, simulationState)
            addExecutedBatch(batchRunSimulationRun)
        }
    }

    public synchronized void batchSimulationStart(Simulation simulation) {
        BatchRunSimulationRun batchRunSimulationRun = update(simulation, SimulationState.RUNNING)
        addExecutedBatch batchRunSimulationRun
    }

    public void addExecutedBatch(BatchRunSimulationRun batchRunSimulationRun) {
        executedBatchRunSimulationRuns.remove(executedBatchRunSimulationRuns.find {it.simulationRun.name == batchRunSimulationRun.simulationRun.name})
        executedBatchRunSimulationRuns << batchRunSimulationRun
    }




    public void addActiveSimulationRun(SimulationRun simulationRun) {
        activeSimulationRuns << simulationRun
    }

    public boolean isBatchActive() {
        return !activeSimulationRuns.isEmpty()
    }

    private BatchRunSimulationRun update(Simulation simulation, SimulationState simulationState) {
        BatchRunSimulationRun batchRunSimulationRun = BatchRunSimulationRun.findBySimulationRun(simulation.simulationRun)
        batchRunSimulationRun.simulationState = simulationState
        batchRunSimulationRun.save()
    }
}

