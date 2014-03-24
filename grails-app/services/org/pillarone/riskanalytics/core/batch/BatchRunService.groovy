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
import org.pillarone.riskanalytics.core.simulation.SimulationState
import org.pillarone.riskanalytics.core.simulation.engine.SimulationConfiguration
import org.pillarone.riskanalytics.core.simulation.engine.SimulationQueueService
import org.pillarone.riskanalytics.core.simulation.item.Simulation

class BatchRunService {

    boolean transactional = false
    BatchRunInfoService batchRunInfoService
    SimulationQueueService simulationQueueService

    @CompileStatic
    public static BatchRunService getService() {
        return Holders.grailsApplication.mainContext.getBean(BatchRunService)
    }

    @CompileStatic
    public void runBatches() {
        activeBatchRuns?.each { BatchRun batchRun ->
            runBatch(batchRun)
        }
    }

    public void runBatch(BatchRun batchRun) {
        getSimulationRuns(batchRun).each { BatchRunSimulationRun batchRunSimulationRun ->
            runSimulation(batchRunSimulationRun)
        }
        BatchRun.withTransaction {
            batchRun.refresh()
            batchRun.executed = true
            batchRun.save(flush: true)
        }
    }

    @CompileStatic
    public synchronized void runSimulation(BatchRunSimulationRun batchRunSimulationRun) {
        if (batchRunSimulationRun.simulationRun.endTime != null) {
            log.info "simulation ${batchRunSimulationRun.simulationRun.name} already executed at ${batchRunSimulationRun.simulationRun.endTime}"
        } else if (batchRunSimulationRun.simulationRun.endTime == null && !batchRunInfoService.runningBatchSimulationRuns.contains(batchRunSimulationRun)) {
            ICollectorOutputStrategy strategy = OutputStrategyFactory.getInstance(batchRunSimulationRun.strategy)
            Simulation simulation = loadSimulation(batchRunSimulationRun.simulationRun.name)
            SimulationConfiguration configuration = new SimulationConfiguration(simulation: simulation, outputStrategy: strategy)
            ImportStructureInTransaction.importStructure(configuration)
            simulationQueueService.offer(configuration)
            notifySimulationStart(simulation, SimulationState.NOT_RUNNING)
        } else {
            log.info "simulation ${batchRunSimulationRun.simulationRun.name} is already running"
        }
    }


    BatchRunSimulationRun addSimulationRun(BatchRun batchRun, Simulation simulation, OutputStrategy strategy) {
        BatchRun.withTransaction {
            simulation.save()
            batchRun = BatchRun.findByName(batchRun.name)
            int priority = BatchRunSimulationRun.countByBatchRun(batchRun)
            BatchRunSimulationRun addedBatchRunSimulationRun = new BatchRunSimulationRun(batchRun: batchRun, simulationRun: simulation.simulationRun, priority: priority, strategy: strategy, simulationState: SimulationState.NOT_RUNNING)
            addedBatchRunSimulationRun.save()
            if (batchRun.executed) {
                batchRun.executed = false
                batchRun.save()
            }

            return addedBatchRunSimulationRun
        }
    }

    SimulationRun getSimulationRunAt(BatchRun batchRun, int index) {
        BatchRun.withTransaction {
            List<SimulationRun> runs = getSimulationRuns(batchRun)*.simulationRun
            return runs?.get(index)
        }
    }

    List<BatchRunSimulationRun> getSimulationRuns(BatchRun batchRun) {
        return BatchRunSimulationRun.findAllByBatchRun(batchRun, [sort: "priority", order: "asc"])
    }

    public BatchRunSimulationRun getSimulationRun(BatchRun batchRun, SimulationRun simulationRun) {
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

    synchronized void changePriority(BatchRun batchRun, SimulationRun simulationRun, int step) {
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

    protected void deleteBatchRunSimulationRun(BatchRunSimulationRun batchRunSimulationRun) {
        batchRunSimulationRun.delete()
        if (batchRunSimulationRun.simulationRun.endTime == null) {
            SimulationRun.get(batchRunSimulationRun.simulationRun.id).delete()
        }
    }

    // Returns the batches that are ready to run.
    // BTW
    // I think column 'executionTime' means: 'Please execute at this time or later'
    // I don't think it means: 'The time it was executed'.
    //
    public List<BatchRun> getActiveBatchRuns() {
        return BatchRun.executeQuery("from org.pillarone.riskanalytics.core.BatchRun as b where b.executed = :executed and b.executionTime <= :cDate order by b.executionTime asc ", ["cDate": new DateTime(), "executed": false])
    }

    public List<BatchRun> getAllBatchRuns() {
        return BatchRun.executeQuery("from org.pillarone.riskanalytics.core.BatchRun as b order by b.executionTime asc")
    }

    @CompileStatic
    private Simulation loadSimulation(String simulationName) {
        Simulation simulation = new Simulation(simulationName)
        simulation.load()
        simulation.parameterization.load();
        simulation.template.load();
        return simulation
    }

    @CompileStatic
    protected void notifySimulationStart(Simulation simulation, SimulationState simulationState) {
        log.info " notifySimulationStart ${simulation.name} : ${simulationState.toString()}"
        batchRunInfoService?.batchSimulationStart(simulation)
    }

}
