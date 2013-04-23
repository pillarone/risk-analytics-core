package org.pillarone.riskanalytics.core.batch

import grails.util.Holders
import groovy.transform.CompileStatic
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.pillarone.riskanalytics.core.BatchRunSimulationRun
import org.pillarone.riskanalytics.core.output.SimulationRun
import org.pillarone.riskanalytics.core.simulation.SimulationState
import org.pillarone.riskanalytics.core.simulation.item.Simulation

/**
 * @author fouad.jaada@intuitive-collaboration.com
 */
class BatchRunInfoService {

    List<BatchRunSimulationRun> runningBatchSimulationRuns
    List<Simulation> finishedSimulations

    @CompileStatic
    public BatchRunInfoService() {
        runningBatchSimulationRuns = []
        finishedSimulations = []
    }

    @CompileStatic
    public static BatchRunInfoService getService() {
        return Holders.applicationContext.getBean(BatchRunInfoService)
    }

    @CompileStatic
    public synchronized void batchSimulationStart(Simulation simulation) {
        BatchRunSimulationRun batchRunSimulationRun = update(simulation, SimulationState.NOT_RUNNING)
        addRunning batchRunSimulationRun
    }

    @CompileStatic
    public synchronized void batchSimulationStateChanged(Simulation simulation, SimulationState simulationState) {
        BatchRunSimulationRun brsr = runningBatchSimulationRuns.find { BatchRunSimulationRun it ->
            (it.simulationRun.name == simulation.name) && (it.simulationRun.model == simulation.modelClass.name)
        }
        if (!brsr) return
        brsr.simulationState = simulationState
        update(simulation, simulationState)
        if (simulationState == SimulationState.FINISHED)
            finishedSimulations << simulation
    }

    @CompileStatic
    public void addRunning(BatchRunSimulationRun batchRunSimulationRun) {
        runningBatchSimulationRuns.remove(runningBatchSimulationRuns.find { BatchRunSimulationRun it ->
            it.simulationRun.name == batchRunSimulationRun.simulationRun.name
        })
        runningBatchSimulationRuns << batchRunSimulationRun
    }

    private BatchRunSimulationRun update(Simulation simulation, SimulationState simulationState) {
        BatchRunSimulationRun batchRunSimulationRun = BatchRunSimulationRun.findBySimulationRun(SimulationRun.get(simulation.id))
        batchRunSimulationRun.simulationState = simulationState
        batchRunSimulationRun.save()
    }

    @CompileStatic
    public BatchRunSimulationRun getBatchRunSimulationRun(BatchRunSimulationRun batchRunSimulationRun) {
        return runningBatchSimulationRuns.find { BatchRunSimulationRun it -> it.id == batchRunSimulationRun.id}
    }

    @CompileStatic
    public List<Simulation> getFinished(long endTime) {
        List<Simulation> simulations = []
        for (Simulation simulation: finishedSimulations) {
            if (simulation.end?.millis > endTime)
                simulations << simulation
        }
        return simulations
    }
}

