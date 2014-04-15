package org.pillarone.riskanalytics.core.batch

import grails.util.Holders
import groovy.transform.CompileStatic
import org.pillarone.riskanalytics.core.BatchRun
import org.pillarone.riskanalytics.core.output.SimulationRun
import org.pillarone.riskanalytics.core.simulation.engine.SimulationConfiguration
import org.pillarone.riskanalytics.core.simulation.engine.SimulationQueueService
import org.pillarone.riskanalytics.core.simulation.item.Simulation

class BatchRunService {

    SimulationQueueService simulationQueueService
    BatchRunInfoService batchRunInfoService
    def backgroundService

    @CompileStatic
    static BatchRunService getService() {
        return Holders.grailsApplication.mainContext.getBean(BatchRunService)
    }

    void runBatch(BatchRun batchRun) {
        BatchRun.withTransaction {
            BatchRun reload = BatchRun.get(batchRun.id)
            offer(reload.simulationRuns)
            reload.executed = true
            reload.save(flush: true)
        }
    }

    void runBatchRunSimulation(SimulationRun simulationRun) {
        offer(simulationRun)
    }

    private boolean shouldRun(SimulationRun run) {
        run.endTime == null && run.startTime == null
    }

    private void offer(List<SimulationRun> simulationRuns) {
        backgroundService.execute("execute bathcRunSimulationRuns $simulationRuns") {
            List<SimulationConfiguration> configurations = simulationRuns.findAll { SimulationRun simulationRun -> shouldRun(simulationRun) }.collect {
                configure(it)
            }
            configurations.each { start(it) }
        }
    }

    private void offer(SimulationRun simulationRun) {
        if (shouldRun(simulationRun)) {
            start(configure(simulationRun))
        }
    }

    private void start(SimulationConfiguration simulationConfiguration) {
        simulationQueueService.offer(simulationConfiguration, 5)
    }

    private SimulationConfiguration configure(SimulationRun simulationRun) {
        def simulation = loadSimulation(simulationRun.name)
        new SimulationConfiguration(simulation)
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

    void deleteSimulationRun(BatchRun batchRun, SimulationRun simulationRun) {
        BatchRun.withTransaction {
            batchRun.attach()
            batchRun.removeFromSimulationRuns(simulationRun)
            batchRun.save(flush: true)
        }
    }

    void changePriority(BatchRun batchRun, SimulationRun simulationRun, int step) {
        BatchRun.withTransaction {
            BatchRun attachedBatchRun = BatchRun.get(batchRun.id)
            List<SimulationRun> simulationRuns = attachedBatchRun.simulationRuns
            def oldPriority = simulationRuns.indexOf(simulationRun)
            if (oldPriority == -1) {
                throw new IllegalStateException("SimulationRun $simulationRun does not belong to batch $attachedBatchRun")
            }
            int newPriority = oldPriority + step
            if (newPriority < 0 || newPriority >= simulationRuns.size()) {
                newPriority = oldPriority
            }
            Collections.swap(attachedBatchRun.simulationRuns, newPriority, oldPriority)
            attachedBatchRun.save(flush: true)
        }
    }

    boolean deleteBatchRun(BatchRun batchRun) {
        BatchRun.withTransaction {
            BatchRun.get(batchRun.id).delete()
        }
        return true
    }
}
