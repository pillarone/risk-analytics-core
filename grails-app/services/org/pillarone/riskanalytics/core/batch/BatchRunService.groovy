package org.pillarone.riskanalytics.core.batch

import grails.util.Holders
import groovy.transform.CompileStatic
import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.BatchRun
import org.pillarone.riskanalytics.core.BatchRunSimulationRun
import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.cli.ImportStructureInTransaction
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
        getSimulationRuns(batchRun).each { BatchRunSimulationRun batchRunSimulationRun ->
            offer(batchRunSimulationRun)
        }
        BatchRun.withTransaction {
            BatchRun reload = BatchRun.get(batchRun.id)
            reload.executed = true
            reload.save(flush: true)
        }
    }

    void runBatchRunSimulation(BatchRun batchRun, SimulationRun simulationRun) {
        offer(getSimulationRun(batchRun, simulationRun))
    }

    List<BatchRun> findBatchRunsWhichShouldBeExecuted() {
        BatchRun.withCriteria {
            eq('executed', false)
            le('executionTime', new DateTime())
            order('executionTime', 'asc')
        }
    }


    void offer(BatchRunSimulationRun batchRunSimulationRun) {
        SimulationRun run = batchRunSimulationRun.simulationRun
        if (run.endTime == null && run.startTime == null) {
            configureAndSendToQueue(batchRunSimulationRun)
        }
        if (run.endTime != null) {
            log.info "simulation ${run.name} already executed at ${run.endTime}"
            return
        }
        log.info "simulation ${batchRunSimulationRun.simulationRun.name} is already running"
    }

    private configureAndSendToQueue(BatchRunSimulationRun batchRunSimulationRun) {
        backgroundService.execute("offering $batchRunSimulationRun to queue") {
            ICollectorOutputStrategy strategy = OutputStrategyFactory.getInstance(batchRunSimulationRun.strategy)
            Simulation simulation = loadSimulation(batchRunSimulationRun.simulationRun.name)
            SimulationConfiguration configuration = new SimulationConfiguration(simulation: simulation, outputStrategy: strategy)
            ImportStructureInTransaction.importStructure(configuration)
            batchRunInfoService.batchSimulationStart(simulation)
            simulationQueueService.offer(configuration, 5)
        }
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
            batchRun = BatchRun.findByName(batchRun.name)
            int priority = BatchRunSimulationRun.countByBatchRun(batchRun)
            BatchRunSimulationRun addedBatchRunSimulationRun = new BatchRunSimulationRun(
                    batchRun: batchRun,
                    simulationRun: simulation.simulationRun,
                    priority: priority,
                    strategy: strategy,
                    simulationState: NOT_RUNNING
            )
            addedBatchRunSimulationRun.save()
            if (batchRun.executed) {
                batchRun.executed = false
                batchRun.save()
            }
            addedBatchRunSimulationRun
        }
    }

    SimulationRun getSimulationRunAt(BatchRun batchRun, int index) {
        BatchRun.withTransaction {
            List<SimulationRun> runs = getSimulationRuns(batchRun)*.simulationRun
            runs?.get(index)
        }
    }

    List<BatchRunSimulationRun> getSimulationRuns(BatchRun batchRun) {
        return BatchRunSimulationRun.findAllByBatchRun(batchRun, [sort: 'priority', order: 'asc'])
    }

    BatchRunSimulationRun getSimulationRun(BatchRun batchRun, SimulationRun simulationRun) {
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
