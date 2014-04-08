package org.pillarone.riskanalytics.core.batch
import grails.util.Holders
import groovy.transform.CompileStatic
import org.pillarone.riskanalytics.core.BatchRun
import org.pillarone.riskanalytics.core.BatchRunSimulationRun
import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.output.ICollectorOutputStrategy
import org.pillarone.riskanalytics.core.output.OutputStrategy
import org.pillarone.riskanalytics.core.output.SimulationRun
import org.pillarone.riskanalytics.core.output.batch.OutputStrategyFactory
import org.pillarone.riskanalytics.core.simulation.engine.SimulationConfiguration
import org.pillarone.riskanalytics.core.simulation.engine.SimulationQueueService
import org.pillarone.riskanalytics.core.simulation.item.Simulation

import static org.pillarone.riskanalytics.core.simulation.SimulationState.NOT_RUNNING

class BatchRunService {

    SimulationQueueService simulationQueueService
    BatchRunInfoService batchRunInfoService
    def backgroundService

    @CompileStatic
    static BatchRunService getService() {
        return Holders.grailsApplication.mainContext.getBean(BatchRunService)
    }

    void runBatch(BatchRun batchRun) {
        offer(getSimulationRuns(batchRun))
        BatchRun.withTransaction {
            BatchRun reload = BatchRun.get(batchRun.id)
            reload.executed = true
            reload.save(flush: true)
        }
    }

    void runBatchRunSimulation(BatchRun batchRun, SimulationRun simulationRun) {
        offer(getSimulationRun(batchRun, simulationRun))
    }

    private boolean shouldRun(BatchRunSimulationRun batchRunSimulationRun) {
        SimulationRun run = batchRunSimulationRun.simulationRun
        run.endTime == null && run.startTime == null
    }

    private void offer(List<BatchRunSimulationRun> batchRunSimulationRuns) {
        backgroundService.execute("execute bathcRunSimulationRuns $batchRunSimulationRuns") {
            List<SimulationConfiguration> configurations = batchRunSimulationRuns.findAll { BatchRunSimulationRun batchRunSimulationRun -> shouldRun(batchRunSimulationRun) }.collect {
                configure(it)
            }
            configurations.each { start(it) }
        }
    }

    private void offer(BatchRunSimulationRun batchRunSimulationRun) {
        if (shouldRun(batchRunSimulationRun)) {
            start(configure(batchRunSimulationRun))
        }
    }

    private void start(SimulationConfiguration simulationConfiguration) {
        batchRunInfoService.batchSimulationStart(simulationConfiguration.simulation)
        simulationQueueService.offer(simulationConfiguration, 5)
    }

    private SimulationConfiguration configure(BatchRunSimulationRun batchRunSimulationRun) {
        ICollectorOutputStrategy strategy = OutputStrategyFactory.getInstance(batchRunSimulationRun.strategy)
        Simulation simulation = loadSimulation(batchRunSimulationRun.simulationRun.name)
        new SimulationConfiguration(simulation: simulation, outputStrategy: strategy)
    }

    private static Simulation loadSimulation(String simulationName) {
        Simulation simulation = new Simulation(simulationName)
        simulation.load()
        simulation.parameterization.load()
        simulation.template.load()
        return simulation
    }

    BatchRunSimulationRun createBatchRunSimulationRun(BatchRun batchRun, Simulation simulation, OutputStrategy strategy) {
        BatchRun.withTransaction {
            simulation.save()
            BatchRun attachedBatchRun = BatchRun.findByName(batchRun.name)
            int priority = BatchRunSimulationRun.countByBatchRun(attachedBatchRun)
            BatchRunSimulationRun addedBatchRunSimulationRun = new BatchRunSimulationRun(
                    batchRun: attachedBatchRun,
                    simulationRun: simulation.simulationRun,
                    priority: priority,
                    strategy: strategy,
                    simulationState: NOT_RUNNING
            )
            addedBatchRunSimulationRun.save(flush: true)
            if (attachedBatchRun.executed) {
                attachedBatchRun.executed = false
                attachedBatchRun.save(flush: true)
            }
            addedBatchRunSimulationRun
        }
    }

    protected List<BatchRunSimulationRun> getSimulationRuns(BatchRun batchRun) {
        return BatchRunSimulationRun.findAllByBatchRun(batchRun, [sort: 'priority', order: 'asc'])
    }

    private BatchRunSimulationRun getSimulationRun(BatchRun batchRun, SimulationRun simulationRun) {
        BatchRunSimulationRun item = null
        BatchRunSimulationRun.withTransaction {
            item = BatchRunSimulationRun.findByBatchRunAndSimulationRun(batchRun, simulationRun)
            item.simulationRun = SimulationRun.findByName(simulationRun.name)
            item.simulationRun.parameterization = ParameterizationDAO.find(simulationRun.parameterization.name, simulationRun.model, simulationRun.parameterization.itemVersion.toString())
        }
        return item
    }

    void deleteSimulationRun(BatchRun batchRun, SimulationRun simulationRun) {
        BatchRun.withTransaction {
            BatchRunSimulationRun batchRunSimulationRun = BatchRunSimulationRun.findByBatchRunAndSimulationRun(batchRun, simulationRun)
            deleteBatchRunSimulationRun(batchRunSimulationRun)
        }
    }

    void changePriority(BatchRun batchRun, SimulationRun simulationRun, int step) {
        BatchRun.withTransaction {
            BatchRunSimulationRun batchRunSimulationRun = BatchRunSimulationRun.findByBatchRunAndSimulationRun(batchRun, simulationRun)
            int newPriority = batchRunSimulationRun.priority + step
            BatchRunSimulationRun bRSRun = BatchRunSimulationRun.findByPriority(newPriority)
            if (bRSRun != null) {
                bRSRun.priority = batchRunSimulationRun.priority
                bRSRun.save()
                batchRunSimulationRun.priority = newPriority
                batchRunSimulationRun.save()
            }
        }
    }

    boolean deleteBatchRun(BatchRun batchRun) {
        BatchRun.withTransaction {
            for (BatchRunSimulationRun toDelete in BatchRunSimulationRun.findAllByBatchRun(batchRun)) {
                deleteBatchRunSimulationRun(toDelete)
            }
            BatchRun.get(batchRun.id).delete()
        }
        return true
    }

    private void deleteBatchRunSimulationRun(BatchRunSimulationRun batchRunSimulationRun) {
        batchRunSimulationRun.delete()
        if (batchRunSimulationRun.simulationRun.endTime == null) {
            SimulationRun.get(batchRunSimulationRun.simulationRun.id).delete()
        }
    }


}
