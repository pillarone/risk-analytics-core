package org.pillarone.riskanalytics.core

import models.core.CoreModel
import org.joda.time.DateTime
import org.junit.Test
import org.pillarone.riskanalytics.core.example.model.EmptyModel
import org.pillarone.riskanalytics.core.output.OutputStrategy
import org.pillarone.riskanalytics.core.output.ResultConfigurationDAO
import org.pillarone.riskanalytics.core.output.batch.BatchRunner
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
        batchRunService.addSimulationRun(batch, simulation, OutputStrategy.BATCH_DB_OUTPUT)

        BatchRun bRun = BatchRun.findByName(batch.name)

        assertTrue batchRunService.getSimulationRuns(bRun).size() == 1
        assertTrue batchRunService.getSimulationRunAt(bRun, 0).name == simulation.name

        Simulation simulation2 = createSimulation("simulation2")
        batchRunService.addSimulationRun(batch, simulation2, OutputStrategy.BATCH_DB_OUTPUT)
        assertTrue batchRunService.getSimulationRunAt(bRun, 1).name == simulation2.name

        assertTrue batchRunService.getSimulationRuns(bRun).size() == 2

        Simulation simulation3 = createSimulation("simulation3")
        batchRunService.addSimulationRun(batch, simulation3, OutputStrategy.BATCH_DB_OUTPUT)
        assertTrue batchRunService.getSimulationRuns(bRun).size() == 3
        assertTrue batchRunService.getSimulationRunAt(bRun, 2).name == simulation3.name

        //change a priority
        batchRunService.changePriority(bRun, simulation3.simulationRun, 1)
        assertTrue batchRunService.getSimulationRunAt(bRun, 2).name == simulation3.name

        batchRunService.changePriority(bRun, simulation3.simulationRun, -1)
        assertTrue batchRunService.getSimulationRunAt(bRun, 2).name == simulation2.name

        batchRunService.changePriority(bRun, simulation3.simulationRun, -1)
        assertTrue batchRunService.getSimulationRunAt(bRun, 1).name == simulation.name

        batchRunService.changePriority(bRun, simulation3.simulationRun, -1)
        assertTrue batchRunService.getSimulationRunAt(bRun, 1).name == simulation.name
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
        batchRunService.addSimulationRun(batch, simulation, OutputStrategy.BATCH_DB_OUTPUT)

        BatchRun bRun = BatchRun.findByName(batch.name)
        assertNotNull bRun
        batchRunService.deleteBatchRun(bRun)

        bRun = BatchRun.findByName(batch.name)
        assertNull bRun
    }

    @Test
    public void testGetActiveBatchRuns() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -2)

        BatchRun batchRun = new BatchRun()
        batchRun.name = "Test1"
        batchRun.executionTime = new DateTime(cal.time.time)
        batchRun.save()

        BatchRun batch = BatchRun.findByName("Test1")

        def bRuns = batchRunService.getSimulationRuns(batch)
        assertTrue bRuns.size() == 0

        Simulation simulation = createSimulation("simulation1")
        batchRunService.addSimulationRun(batch, simulation, OutputStrategy.BATCH_DB_OUTPUT)

        List<BatchRun> batchRuns = BatchRunner.getService().getActiveBatchRuns()
        assertNotNull batchRuns
        assertTrue batchRuns.size() == 1
        BatchRun bRun0 = batchRuns.get(0)
        assertNotNull bRun0
        List<BatchRunSimulationRun> brsr = batchRunService.getSimulationRuns(bRun0)
        assertNotNull brsr
        assertTrue brsr.size() == 1

        simulation = createSimulation("simulation2")
        batchRunService.addSimulationRun(batch, simulation, OutputStrategy.BATCH_DB_OUTPUT)

        brsr = batchRunService.getSimulationRuns(bRun0)
        assertTrue brsr.size() == 2
        assertTrue brsr.get(0).simulationRun.name == "simulation1"
        assertTrue brsr.get(1).simulationRun.name == "simulation2"

        cal.add(Calendar.DAY_OF_MONTH, 1)
        BatchRun batchRun2 = new BatchRun()
        batchRun2.name = "Test2"
        batchRun2.executionTime = new DateTime(cal.time.time)
        batchRun2.save()

        batchRuns = BatchRunner.getService().getActiveBatchRuns()
        assertTrue batchRuns.size() == 2
        assertTrue batchRuns.get(0).name == "Test1"
        assertTrue batchRuns.get(1).name == "Test2"

        cal.add(Calendar.DAY_OF_MONTH, 2)
        BatchRun batchRun3 = new BatchRun()
        batchRun3.name = "Test3"
        batchRun3.executionTime = new DateTime(cal.time.time)
        batchRun3.save()

        batchRuns = BatchRunner.getService().getActiveBatchRuns()
        assertTrue batchRuns.size() == 2
        assertTrue batchRuns.get(0).name == "Test1"
        assertTrue batchRuns.get(1).name == "Test2"

    }

    private Simulation createSimulation(String simulationName) {
        createParameterization()
        createResultConfiguration()
        Simulation simulation = new Simulation(simulationName)
        simulation.parameterization = new Parameterization("params")
        simulation.template = new ResultConfiguration("template")
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
