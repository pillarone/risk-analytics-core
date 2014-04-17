package org.pillarone.riskanalytics.core.batch

import grails.util.Holders
import groovy.transform.CompileStatic
import org.pillarone.riskanalytics.core.BatchRun
import org.pillarone.riskanalytics.core.output.SimulationRun
import org.pillarone.riskanalytics.core.simulation.engine.SimulationConfiguration
import org.pillarone.riskanalytics.core.simulation.engine.SimulationQueueService
import org.pillarone.riskanalytics.core.simulation.item.Batch
import org.pillarone.riskanalytics.core.simulation.item.Simulation

class BatchRunService {

    SimulationQueueService simulationQueueService
    BatchRunInfoService batchRunInfoService
    def backgroundService

    @CompileStatic
    static BatchRunService getService() {
        return Holders.grailsApplication.mainContext.getBean(BatchRunService)
    }

    void runBatch(Batch batch) {
        BatchRun.withTransaction {
            BatchRun batchRun = BatchRun.findByName(batch.name)
            offer(batch.simulations)
            batchRun.executed = true
            batchRun.save(flush: true)
        }
    }

    void runBatchRunSimulation(Simulation simulationRun) {
        offer(simulationRun)
    }

    private boolean shouldRun(Simulation run) {
        run.end == null && run.start == null
    }

    private void offer(List<Simulation> simulationRuns) {
        backgroundService.execute("execute bathcRunSimulationRuns $simulationRuns") {
            List<SimulationConfiguration> configurations = simulationRuns.findAll { Simulation simulationRun -> shouldRun(simulationRun) }.collect {
                configure(it)
            }
            configurations.each { start(it) }
        }
    }

    private void offer(Simulation simulation) {
        if (shouldRun(simulation)) {
            start(configure(simulation))
        }
    }

    private void start(SimulationConfiguration simulationConfiguration) {
        simulationQueueService.offer(simulationConfiguration, 5)
    }

    private SimulationConfiguration configure(Simulation simulation) {
        new SimulationConfiguration(loadSimulation(simulation.name))
    }

    private static Simulation loadSimulation(String simulationName) {
        Simulation simulation = new Simulation(simulationName)
        simulation.load()
        simulation.parameterization.load()
        simulation.template.load()
        return simulation
    }

    int createBatchRunSimulationRun(BatchRun batchRun, Simulation simulation) {
        BatchRun.withTransaction {
            simulation.save()
            BatchRun attachedBatchRun = BatchRun.findByName(batchRun.name)
            attachedBatchRun.addToSimulationRuns(simulation.simulationRun)
            attachedBatchRun.executed = false
            attachedBatchRun.save(flush: true)
            attachedBatchRun.simulationRuns.size() - 1
        }
    }

    void deleteSimulationRun(Batch batch, Simulation simulation) {
        BatchRun.withTransaction {
            BatchRun batchRun = BatchRun.lock(batch.id)
            SimulationRun simulationRun = SimulationRun.lock(simulation.id)
            batchRun.removeFromSimulationRuns(simulationRun)
            batchRun.save(flush: true)
        }
    }

    void changePriority(Batch batch, Simulation simulation, int step) {
        batch.load()
        List<Simulation> simulationRuns = batch.simulations
        def oldPriority = simulationRuns.indexOf(simulation)
        if (oldPriority == -1) {
            throw new IllegalStateException("SimulationRun $simulation does not belong to batch $batch")
        }
        int newPriority = oldPriority + step
        if (newPriority < 0 || newPriority >= simulationRuns.size()) {
            newPriority = oldPriority
        }
        BatchRun.withTransaction {
            BatchRun batchRun = BatchRun.get(batch.id)
            Collections.swap(batchRun.simulationRuns, newPriority, oldPriority)
            Collections.swap(batch.simulations, newPriority, oldPriority)
            batchRun.save(flush: true)
        }
    }

    boolean deleteBatch(Batch batch) {
        batch.delete()
    }
}
