package org.pillarone.riskanalytics.core

import models.core.CoreModel
import org.junit.Before
import org.junit.Test
import org.pillarone.riskanalytics.core.batch.BatchRunService
import org.pillarone.riskanalytics.core.fileimport.FileImportService
import org.pillarone.riskanalytics.core.output.SimulationRun
import org.pillarone.riskanalytics.core.simulation.item.Batch
import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import org.pillarone.riskanalytics.core.simulation.item.ResultConfiguration
import org.pillarone.riskanalytics.core.simulation.item.Simulation

import static org.junit.Assert.*

/**
 * @author fouad jaada
 */

class BatchRunTests {

    BatchRunService batchRunService

    @Before
    void setUp() {
        FileImportService.importModelsIfNeeded(['Core'])
    }

    @Test
    public void testAddSimulationRun() {
        BatchRun batchRun = new BatchRun()
        batchRun.name = "Test"
        batchRun.save()

        BatchRun batch = BatchRun.findByName("Test")
        assertNotNull batch

        def bRuns = batch.simulationRuns
        assertTrue bRuns.size() == 0

        Simulation simulation = createSimulation("simulation1")
        batchRunService.createBatchRunSimulationRun(batch, simulation)

        BatchRun bRun = BatchRun.findByName(batch.name)
        Batch bat = new Batch(bRun.name)
        bat.load()
        assertTrue bRun.simulationRuns.size() == 1
        assertTrue getSimulationRunAt(bRun.name, 0).name == simulation.name

        Simulation simulation2 = createSimulation("simulation2")
        batchRunService.createBatchRunSimulationRun(batch, simulation2)
        assertTrue getSimulationRunAt(bRun.name, 1).name == simulation2.name

        assertTrue bRun.simulationRuns.size() == 2

        Simulation simulation3 = createSimulation("simulation3")
        batchRunService.createBatchRunSimulationRun(batch, simulation3)
        assertTrue bRun.simulationRuns.size() == 3
        assertTrue getSimulationRunAt(bRun.name, 2).name == simulation3.name

        //change a priority
        batchRunService.changePriority(bat, simulation3, 1)
        bRun = BatchRun.findByName(batch.name)
        assertTrue getSimulationRunAt(bRun.name, 2).name == simulation3.name

        batchRunService.changePriority(bat, simulation3, -1)
        assertTrue getSimulationRunAt(bRun.name, 2).name == simulation2.name

        batchRunService.changePriority(bat, simulation3, -1)
        assertTrue getSimulationRunAt(bRun.name, 1).name == simulation.name

        batchRunService.changePriority(bat, simulation3, -1)
        assertTrue getSimulationRunAt(bRun.name, 1).name == simulation.name
    }

    private SimulationRun getSimulationRunAt(String batchRunName, int index) {
        BatchRun.withTransaction {
            BatchRun batchRun = BatchRun.findByName(batchRunName)
            List<SimulationRun> runs = batchRun.simulationRuns
            runs?.get(index)
        }
    }

    @Test
    public void testDeleteBatchRun() {
        BatchRun batchRun = new BatchRun()
        batchRun.name = "TestDeleteMe"
        batchRun.save()
        batchRun = BatchRun.findByName(batchRun.name)
        assertTrue batchRun.simulationRuns.size() == 0

        Simulation simulation = createSimulation("simulation1")
        batchRunService.createBatchRunSimulationRun(batchRun, simulation)

        BatchRun bRun = BatchRun.findByName(batchRun.name)
        assertNotNull bRun

        Batch batch = new Batch(batchRun.name)

        batchRunService.deleteBatch(batch)

        bRun = BatchRun.findByName(batch.name)
        assertNull bRun
    }

    private Simulation createSimulation(String simulationName) {
        Simulation simulation = new Simulation(simulationName)
        simulation.parameterization = new Parameterization("CoreParameters", CoreModel)
        simulation.template = new ResultConfiguration("CoreResultConfiguration", CoreModel)
        simulation.periodCount = 1
        simulation.numberOfIterations = 10
        simulation.modelClass = CoreModel
        return simulation
    }
}
