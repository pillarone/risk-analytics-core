package org.pillarone.riskanalytics.core.output

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.pillarone.riskanalytics.core.BatchRun
import org.pillarone.riskanalytics.core.BatchRunSimulationRun
import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.output.batch.OutputStrategyFactory
import org.pillarone.riskanalytics.core.simulation.SimulationState
import org.pillarone.riskanalytics.core.simulation.engine.RunSimulationService
import org.pillarone.riskanalytics.core.simulation.engine.SimulationConfiguration
import org.pillarone.riskanalytics.core.simulation.engine.SimulationRunner
import org.pillarone.riskanalytics.core.simulation.item.Simulation

class BatchRunService {

    boolean transactional = true
    Log LOG = LogFactory.getLog(BatchRunService)

    public void runBatches() {
        getActiveBatchRuns()?.each {BatchRun batchRun ->
            runBatch(batchRun)
        }
    }

    public void runBatch(BatchRun batchRun) {
        batchRun = BatchRun.findByName(batchRun?.name)
        getSimulationRuns(batchRun)?.each {BatchRunSimulationRun batchRunSimulationRun ->
            runSimulation(batchRunSimulationRun)
        }
        batchRun.executed = true
        batchRun.save()
    }

    public synchronized void runSimulation(BatchRunSimulationRun batchRunSimulationRun) {
        LOG.info "executing a simulation ${batchRunSimulationRun.simulationRun.name} at ${new Date()}"
        if (batchRunSimulationRun.simulationRun.endTime == null) {
            long currentTime = System.currentTimeMillis()
            ICollectorOutputStrategy strategy = OutputStrategyFactory.getInstance(batchRunSimulationRun.strategy)

            SimulationRunner runner = SimulationRunner.createRunner()
            Simulation simulation = createSimulation(batchRunSimulationRun.simulationRun.name)
            SimulationConfiguration configuration = new SimulationConfiguration(simulation: simulation, outputStrategy: strategy)

            RunSimulationService.getService().runSimulation(runner, configuration)

            batchRunSimulationRun.simulationState = runner.getSimulationState()
            batchRunSimulationRun.save()
            LOG.info "simulation ${batchRunSimulationRun.simulationRun.name} executed, it tooks ${System.currentTimeMillis() - currentTime}"
        } else {
            LOG.info "simulation ${batchRunSimulationRun.simulationRun.name} is already executed at ${batchRunSimulationRun.simulationRun.endTime}"
        }
    }


    void addSimulationRun(BatchRun batchRun, Simulation simulation, OutputStrategy strategy) {
        BatchRun.withTransaction {
            simulation.save()
            batchRun = BatchRun.findByName(batchRun.name)
            int priority = BatchRunSimulationRun.countByBatchRun(batchRun)
            new BatchRunSimulationRun(batchRun: batchRun, simulationRun: simulation.simulationRun, priority: priority, strategy: strategy, simulationState: SimulationState.NOT_RUNNING).save()
            if (batchRun.executed) {
                batchRun.executed = false
                batchRun.save()
            }
        }
    }

    SimulationRun getSimulationRunAt(BatchRun batchRun, int index) {
        BatchRun.withTransaction {
            List<SimulationRun> runs = getSimulationRuns(batchRun)*.simulationRun
            return runs?.get(index)
        }
    }

    List<BatchRunSimulationRun> getSimulationRuns(BatchRun batchRun) {
        return BatchRunSimulationRun.findAllByBatchRunAndSimulationState(batchRun, SimulationState.NOT_RUNNING, [sort: "priority", order: "asc"])
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
            batchRunSimulationRun.delete()
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
            try {
                List<BatchRunSimulationRun> batchRunSimulationRuns = BatchRunSimulationRun.findAllByBatchRun(batchRun)
                batchRunSimulationRuns.each {BatchRunSimulationRun batchRunSimulationRun ->
                    batchRunSimulationRun.delete()
                }
                batchRun.delete()
                return true
            } catch (Exception ex) {
                LOG.error "Exception occured during delete of BatchRun : ${batchRun.name}"
            }
            return false
        }
    }


    public List<BatchRun> getActiveBatchRuns() {
        return BatchRun.executeQuery("from org.pillarone.riskanalytics.core.BatchRun as b where b.executed = :executed and b.executionTime <= :cDate order by b.executionTime asc ", ["cDate": new Date(), "executed": false])
    }

    public List<BatchRun> getAllBatchRuns() {
        return BatchRun.executeQuery("from org.pillarone.riskanalytics.core.BatchRun as b order by b.executionTime asc")
    }

    private Simulation createSimulation(String simulationName) {
        Simulation simulation = new Simulation(simulationName)
        simulation.load()
        simulation.getParameterization().load();
        simulation.getTemplate().load();
        return simulation
    }

}
