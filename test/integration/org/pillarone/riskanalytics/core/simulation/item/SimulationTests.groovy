package org.pillarone.riskanalytics.core.simulation.item

import models.core.CoreModel
import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.BatchRun
import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.example.model.EmptyModel
import org.pillarone.riskanalytics.core.fileimport.FileImportService
import org.pillarone.riskanalytics.core.parameter.comment.ResultCommentDAO
import org.pillarone.riskanalytics.core.simulation.item.parameter.comment.Comment
import org.pillarone.riskanalytics.core.workflow.Status
import org.pillarone.riskanalytics.core.output.*

class SimulationTests extends GroovyTestCase {

    DeleteSimulationService deleteSimulationService

    protected void setUp() {
        super.setUp()
        FileImportService.importModelsIfNeeded(["Core"])
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

    void testLoad() {
        DateTime start = new DateTime()
        SimulationRun run = new SimulationRun()
        run.name = "simulation"
        run.parameterization = createParameterization()
        run.resultConfiguration = createResultConfiguration()
        run.model = CoreModel.name
        run.periodCount = 1
        run.iterations = 10
        DateTime end = new DateTime()
        run.startTime = start
        run.endTime = end
        run.save()

        Simulation simulation = new Simulation("simulation")
        simulation.load()
        assertNotNull simulation.parameterization
        assertEquals "params", simulation.parameterization.name
        assertNotNull simulation.template
        assertEquals "template", simulation.template.name
        assertNotNull simulation.structure
        assertEquals "CoreStructure", simulation.structure.name
        assertEquals 1, simulation.periodCount
        assertEquals 10, simulation.numberOfIterations
        assertSame CoreModel, simulation.modelClass
        assertEquals start, simulation.start
        assertEquals end, simulation.end
        assertEquals run.id, simulation.dao.id

        assertNotNull simulation.modelClass
        assertEquals simulation.modelClass, simulation.parameterization.modelClass
    }

    void testSave() {
        createParameterization()
        createResultConfiguration()
        Simulation simulation = new Simulation("newSimulation")
        simulation.parameterization = new Parameterization("params")
        simulation.template = new ResultConfiguration("template")
        simulation.periodCount = 1
        simulation.numberOfIterations = 10

        assertNull "modelClass missing. Simulation should not be saved", simulation.save()
        simulation.modelClass = CoreModel
        assertNotNull "Simulation complete, should be saved", simulation.save()
    }

    void testDelete() {
        new DBCleanUpService().cleanUp()

        SimulationRun run1 = new SimulationRun()
        run1.name = "simulation1"
        run1.parameterization = createParameterization()
        run1.resultConfiguration = createResultConfiguration()
        run1.model = CoreModel.name
        run1.periodCount = 1
        run1.iterations = 10
        run1.save()

        SimulationRun run2 = new SimulationRun()
        run2.name = "simulation2"
        run2.parameterization = createParameterization()
        run2.resultConfiguration = createResultConfiguration()
        run2.model = CoreModel.name
        run2.periodCount = 1
        run2.iterations = 10
        run2.save()

        PathMapping path = new PathMapping(pathName: "model:path").save()
        CollectorMapping collector = new CollectorMapping(collectorName: "collector").save()
        FieldMapping field = new FieldMapping(fieldName: "field").save()

        3.times {
            new SingleValueResult(simulationRun: run1, iteration: 0, period: 0, value: 1.1, collector: collector, path: path, field: field).save()
        }
        2.times {
            new SingleValueResult(simulationRun: run2, iteration: 0, period: 0, value: 1.1, collector: collector, path: path, field: field).save()
        }

        assertNotNull new PostSimulationCalculation(run: run1, keyFigure: PostSimulationCalculation.MEAN, collector: collector, path: path, field: field, period: 0, result: 0).save()

        def oldParametrization = run1.parameterization // store for later use
        def oldSimTemplate = run1.resultConfiguration

        Simulation simulation = new Simulation("simulation1")
        assertTrue "Simulation deleted", simulation.delete()

        assertEquals 3, SingleValueResult.list().findAll {svr -> svr.simulationRun.toBeDeleted}.size()

        assertNotNull "simulation toBeDeleted == true ", SimulationRun.findByToBeDeleted(true)

        // these deletions should be possible without foreign key constraint violations
        oldParametrization.delete(flush: true)
        oldSimTemplate.delete(flush: true)

        // test physical deletion

        deleteSimulationService.deleteAllMarkedSimulations()
        assertNull SimulationRun.findByToBeDeleted(true)
    }

    void deleteSimulationAndBacthRunSimulationRun() {
        new DBCleanUpService().cleanUp()

        BatchRun batchRun = new BatchRun()
        batchRun.name = "Test"
        batchRun.executionTime = new DateTime()
        batchRun.save()

        BatchRun batch = BatchRun.findByName("Test")
        assertNotNull batch

        def bRuns = batch.batchRunService.getSimulationRuns(batch)
        assertTrue bRuns.size() == 0

        Simulation simulation = createSimulation("simulation")
        batch.batchRunService.addSimulationRun(batch, simulation, OutputStrategy.BATCH_DB_OUTPUT)

        BatchRun bRun = BatchRun.findByName(batch.name)

        assertTrue batch.batchRunService.getSimulationRuns(bRun).size() == 1
        assertTrue batchRun.batchRunService.getSimulationRunAt(bRun, 0).name == simulation.name

        assertTrue "Simulation deleted", simulation.delete()
        assertNotNull "simulation toBeDeleted == true ", SimulationRun.findByToBeDeleted(true)

        deleteSimulationService.deleteAllMarkedSimulations()
        assertNull SimulationRun.findByToBeDeleted(true)
    }

    void testAddRemoveComment() {
        Simulation simulation = createSimulation("Tests")
        simulation.periodCount = 1
        simulation.modelClass = EmptyModel

        int initialCount = ResultCommentDAO.count()

        Comment newComment = new Comment("path", 0)
        newComment.text = "text"

        simulation.addComment(newComment)
        simulation.removeComment(newComment)

        simulation.save()

        assertEquals 0, simulation.comments.size()
        assertEquals 0, simulation.getSize(null)
        assertEquals initialCount, ResultCommentDAO.count()

        simulation.addComment(newComment)

        simulation.save()

        assertEquals 1, simulation.comments.size()
        assertEquals initialCount + 1, simulation.getSize(null)
        assertEquals initialCount + 1, ResultCommentDAO.count()

        simulation.removeComment(newComment)

        simulation.save()

        assertEquals 0, simulation.comments.size()
        assertEquals initialCount, ResultCommentDAO.count()
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


}
