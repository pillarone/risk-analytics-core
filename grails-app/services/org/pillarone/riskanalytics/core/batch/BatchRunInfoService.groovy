package org.pillarone.riskanalytics.core.batch

import grails.util.Holders
import groovy.transform.CompileStatic
import org.pillarone.riskanalytics.core.BatchRunSimulationRun
import org.pillarone.riskanalytics.core.output.SimulationRun
import org.pillarone.riskanalytics.core.simulation.SimulationState
import org.pillarone.riskanalytics.core.simulation.engine.ISimulationQueueListener
import org.pillarone.riskanalytics.core.simulation.engine.QueueEntry
import org.pillarone.riskanalytics.core.simulation.engine.SimulationQueueService
import org.pillarone.riskanalytics.core.simulation.item.Simulation

import javax.annotation.PostConstruct

import static org.pillarone.riskanalytics.core.simulation.SimulationState.NOT_RUNNING

class BatchRunInfoService {

    SimulationQueueService simulationQueueService

    private final List<BatchRunSimulationRun> runningBatchSimulationRuns
    private final Object lock = new Object()

    BatchRunInfoService() {
        runningBatchSimulationRuns = []
    }

    @PostConstruct
    void initialize() {
        simulationQueueService.addSimulationQueueListener(new MySimulationListener())
    }

    @CompileStatic
    static BatchRunInfoService getService() {
        return Holders.grailsApplication.mainContext.getBean(BatchRunInfoService)
    }


    @CompileStatic
    void batchSimulationStateChanged(Simulation simulation, SimulationState simulationState) {
        synchronized (lock) {
            BatchRunSimulationRun batchRunSimulationRun = runningBatchSimulationRuns.find { BatchRunSimulationRun it ->
                (it.simulationRun.name == simulation.name) && (it.simulationRun.model == simulation.modelClass.name)
            }
            if (!batchRunSimulationRun) {
                return
            }
            batchRunSimulationRun.simulationState = simulationState

            update(simulation, simulationState)
        }
    }

    @CompileStatic
    SimulationState getSimulationState(BatchRunSimulationRun batchRunSimulationRun) {
        synchronized (lock) {
            def run = runningBatchSimulationRuns.find { BatchRunSimulationRun it -> it.id == batchRunSimulationRun.id }
            run ? run.simulationState : null
        }
    }

    @CompileStatic
    private void batchSimulationStart(Simulation simulation) {
        synchronized (lock) {
            BatchRunSimulationRun batchRunSimulationRun = update(simulation, NOT_RUNNING)
            addRunning batchRunSimulationRun
        }
    }

    @CompileStatic
    private void addRunning(BatchRunSimulationRun batchRunSimulationRun) {
        runningBatchSimulationRuns.remove(runningBatchSimulationRuns.find { BatchRunSimulationRun it ->
            it.simulationRun.name == batchRunSimulationRun.simulationRun.name
        })
        runningBatchSimulationRuns << batchRunSimulationRun
    }

    private BatchRunSimulationRun update(Simulation simulation, SimulationState simulationState) {
        BatchRunSimulationRun batchRunSimulationRun = BatchRunSimulationRun.findBySimulationRun(SimulationRun.get(simulation.id as Long))
        batchRunSimulationRun.simulationState = simulationState
        batchRunSimulationRun.save()
    }

    private class MySimulationListener implements ISimulationQueueListener {

        @Override
        void started(QueueEntry entry) {}

        @Override
        void finished(QueueEntry entry) {}

        @Override
        void offered(QueueEntry entry) {
            Simulation simulation = entry.simulationConfiguration.simulation
            if (BatchRunSimulationRun.findBySimulationRun(SimulationRun.get(simulation.id as Long))) {
                batchSimulationStart(simulation)
            }
        }

        @Override
        void removed(UUID id) {}
    }
}

