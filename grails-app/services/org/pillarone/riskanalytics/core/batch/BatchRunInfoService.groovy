package org.pillarone.riskanalytics.core.batch

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.pillarone.riskanalytics.core.BatchRunSimulationRun
import org.pillarone.riskanalytics.core.simulation.SimulationState
import org.pillarone.riskanalytics.core.simulation.item.Simulation

/**
 * @author fouad.jaada@intuitive-collaboration.com
 */
class BatchRunInfoService {

    List<BatchRunSimulationRun> runningBatchSimulationRuns
    List<BatchRunSimulationRun> finishedSimulations

    Log LOG = LogFactory.getLog(BatchRunInfoService)

    public BatchRunInfoService() {
        runningBatchSimulationRuns = []
        finishedSimulations = []
    }

    public static BatchRunInfoService getService() {
        return ApplicationHolder.getApplication().getMainContext().getBean('batchRunInfoService')
    }


    public synchronized void batchSimulationStart(Simulation simulation) {
        BatchRunSimulationRun batchRunSimulationRun = update(simulation, SimulationState.NOT_RUNNING)
        addRunning batchRunSimulationRun
    }


    public synchronized void batchSimulationStateChanged(Simulation simulation, SimulationState simulationState) {
        BatchRunSimulationRun brsr = runningBatchSimulationRuns.find { (it.simulationRun.name == simulation.name) && (it.simulationRun.model == simulation.modelClass.name)}
        if (!brsr) return
        brsr.simulationState = simulationState
        update(simulation, simulationState)
        if (simulationState == SimulationState.FINISHED)
            finishedSimulations << simulation
    }


    public void addRunning(BatchRunSimulationRun batchRunSimulationRun) {
        runningBatchSimulationRuns.remove(runningBatchSimulationRuns.find {it.simulationRun.name == batchRunSimulationRun.simulationRun.name})
        runningBatchSimulationRuns << batchRunSimulationRun
    }


    private BatchRunSimulationRun update(Simulation simulation, SimulationState simulationState) {
        BatchRunSimulationRun batchRunSimulationRun = BatchRunSimulationRun.findBySimulationRun(simulation.simulationRun)
        batchRunSimulationRun.simulationState = simulationState
        batchRunSimulationRun.save()
    }

    public BatchRunSimulationRun getBatchRunSimulationRun(BatchRunSimulationRun batchRunSimulationRun) {
        return runningBatchSimulationRuns.find { it.id == batchRunSimulationRun.id}
    }

    public List<Simulation> getFinished(long endTime) {
        List<Simulation> simulations = []
        for (Simulation simulation: finishedSimulations) {
            if (simulation?.getEnd()?.getTime() > endTime)
                simulations << simulation
        }
        return simulations
    }
}

