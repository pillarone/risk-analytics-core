package org.pillarone.riskanalytics.core
import models.core.CoreModel
import org.joda.time.DateTime
import org.junit.Test
import org.pillarone.riskanalytics.core.example.model.EmptyModel
import org.pillarone.riskanalytics.core.output.OutputStrategy
import org.pillarone.riskanalytics.core.output.ResultConfigurationDAO
import org.pillarone.riskanalytics.core.output.SimulationRun
import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import org.pillarone.riskanalytics.core.simulation.item.ResultConfiguration
import org.pillarone.riskanalytics.core.simulation.item.Simulation
import org.pillarone.riskanalytics.core.workflow.Status

import static org.junit.Assert.*
/**
 * @author fouad jaada
 */

class BatchRunTests {

    def batchRunService

    @Test
    public void testAddSimulationRun() {
        BatchRun batchRun = new BatchRun()
        batchRun.name = "Test"
        batchRun.executionTime = new DateTime()
        batchRun.save()

        BatchRun batch = BatchRun.findByName("Test")
        assertNotNull batch

        def bRuns = batchRunService.getSimulationRuns(batch)
        assertTrue bRuns.size() == 0

        Simulation simulation = createSimulation("simulation1")
        batchRunService.createBatchRunSimulationRun(batch, simulation, OutputStrategy.BATCH_DB_OUTPUT)

        BatchRun bRun = BatchRun.findByName(batch.name)

        assertTrue batchRunService.getSimulationRuns(bRun).size() == 1
        assertTrue getSimulationRunAt(bRun, 0).name == simulation.name

        Simulation simulation2 = createSimulation("simulation2")
        batchRunService.createBatchRunSimulationRun(batch, simulation2, OutputStrategy.BATCH_DB_OUTPUT)
        assertTrue getSimulationRunAt(bRun, 1).name == simulation2.name

        assertTrue batchRunService.getSimulationRuns(bRun).size() == 2

        Simulation simulation3 = createSimulation("simulation3")
        batchRunService.createBatchRunSimulationRun(batch, simulation3, OutputStrategy.BATCH_DB_OUTPUT)
        assertTrue batchRunService.getSimulationRuns(bRun).size() == 3
        assertTrue getSimulationRunAt(bRun, 2).name == simulation3.name

        //change a priority
        batchRunService.changePriority(bRun, simulation3.simulationRun, 1)
        assertTrue getSimulationRunAt(bRun, 2).name == simulation3.name

        batchRunService.changePriority(bRun, simulation3.simulationRun, -1)
        assertTrue getSimulationRunAt(bRun, 2).name == simulation2.name

        batchRunService.changePriority(bRun, simulation3.simulationRun, -1)
        assertTrue getSimulationRunAt(bRun, 1).name == simulation.name

        batchRunService.changePriority(bRun, simulation3.simulationRun, -1)
        assertTrue getSimulationRunAt(bRun, 1).name == simulation.name
    }

    private SimulationRun getSimulationRunAt(BatchRun batchRun, int index) {
        BatchRun.withTransaction {
            List<SimulationRun> runs = batchRunService.getSimulationRuns(batchRun)*.simulationRun
            runs?.get(index)
        }
    }

    @Test
    public void testDeleteBatchRun() {

        BatchRun batchRun = new BatchRun()
        batchRun.name = "Test"
        batchRun.executionTime = new DateTime()
        batchRun.save()

        BatchRun batch = BatchRun.findByName("Test")

        def bRuns = batchRunService.getSimulationRuns(batch)
        assertTrue bRuns.size() == 0

        Simulation simulation = createSimulation("simulation1")
        batchRunService.createBatchRunSimulationRun(batch, simulation, OutputStrategy.BATCH_DB_OUTPUT)

        BatchRun bRun = BatchRun.findByName(batch.name)
        assertNotNull bRun
        batchRunService.deleteBatchRun(bRun)

        bRun = BatchRun.findByName(batch.name)
        assertNull bRun
    }

    private Simulation createSimulation(String simulationName) {
        createParameterization()
        createResultConfiguration()
        Simulation simulation = new Simulation(simulationName)
        simulation.parameterization = new Parameterization("params")
        simulation.template = new ResultConfiguration("template", EmptyModel)
        simulation.periodCount = 1
        simulation.numberOfIterations = 10
        simulation.modelClass = EmptyModel
        return simulation
    }

    protected ParameterizationDAO createParameterization() {
        ParameterizationDAO params = new ParameterizationDAO(name: 'params')
        params.modelClassName = CoreModel.name
        params.itemVersion = '1'
        params.periodCount = 1
        params.status = Status.NONE
        assertNotNull "params not saved", params.save()
        params
    }

    protected ResultConfigurationDAO createResultConfiguration() {
        ResultConfigurationDAO template = new ResultConfigurationDAO(name: 'template')
        template.modelClassName = CoreModel.name
        template.itemVersion = '1'
        assertNotNull "template not saved", template.save()
        template
    }


}
