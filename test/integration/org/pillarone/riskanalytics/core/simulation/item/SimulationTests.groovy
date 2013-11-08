package org.pillarone.riskanalytics.core.simulation.item

import models.core.CoreModel
import org.joda.time.DateTime
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.pillarone.riskanalytics.core.ModelDAO
import org.pillarone.riskanalytics.core.ModelStructureDAO
import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.example.model.EmptyModel
import org.pillarone.riskanalytics.core.fileimport.FileImportService
import org.pillarone.riskanalytics.core.output.*
import org.pillarone.riskanalytics.core.parameter.IntegerParameter
import org.pillarone.riskanalytics.core.parameter.Parameter
import org.pillarone.riskanalytics.core.parameter.SimulationTag
import org.pillarone.riskanalytics.core.parameter.comment.ResultCommentDAO
import org.pillarone.riskanalytics.core.parameter.comment.Tag
import org.pillarone.riskanalytics.core.simulation.item.parameter.IntegerParameterHolder
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolderFactory
import org.pillarone.riskanalytics.core.simulation.item.parameter.comment.Comment
import org.pillarone.riskanalytics.core.simulation.item.parameter.comment.EnumTagType
import org.pillarone.riskanalytics.core.simulation.item.parameter.comment.FunctionComment
import org.pillarone.riskanalytics.core.workflow.Status

import static org.junit.Assert.*

class SimulationTests {


    @Before
    void setUp() {
        FileImportService.importModelsIfNeeded(["Core"])
    }

    @After
    void cleanUp() {
        SimulationRun.list()*.delete()
        ParameterizationDAO.list()*.delete()
        ResultConfigurationDAO.list()*.delete()
        ModelStructureDAO.list()*.delete()
        ModelDAO.list()*.delete()
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

    @Test
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
        run.addToRuntimeParameters(new IntegerParameter(path: "path", periodIndex: 1, integerValue: 50))
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

        assertEquals 1, simulation.runtimeParameters.size()

        IntegerParameterHolder holder = simulation.runtimeParameters[0]
        assertEquals "path", holder.path
        assertEquals 1, holder.periodIndex
        assertEquals 50, holder.businessObject

        assertNotNull simulation.modelClass
        assertEquals simulation.modelClass, simulation.parameterization.modelClass
    }

    @Test
    void testSave() {
        createParameterization()
        createResultConfiguration()

        int parameterCount = Parameter.count()

        Simulation simulation = new Simulation("newSimulation")
        simulation.parameterization = new Parameterization("params")
        simulation.template = new ResultConfiguration("template")
        simulation.periodCount = 1
        simulation.numberOfIterations = 10

        assertNull "modelClass missing. Simulation should not be saved", simulation.save()
        simulation.modelClass = CoreModel

        simulation.addParameter(ParameterHolderFactory.getHolder("path", 0, 1))
        simulation.addParameter(ParameterHolderFactory.getHolder("path2", 0, "string"))

        assertNotNull "Simulation complete, should be saved", simulation.save()

        assertEquals parameterCount + 2, Parameter.count()
    }

    @Test
    void testDelete() {
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

        assertEquals 2, SingleValueResult.count()
        assertEquals 1, SimulationRun.count()

        // these deletions should be possible without foreign key constraint violations
        oldParametrization.delete(flush: true)
        oldSimTemplate.delete(flush: true)

    }

    @Test
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

    @Test
    void testAddRemoveFunctionComment() {
        Simulation simulation = createSimulation("Tests")
        simulation.periodCount = 1
        simulation.modelClass = EmptyModel

        int initialCount = ResultCommentDAO.count()

        FunctionComment newComment = new FunctionComment("path", 0, "Min")
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

    @Test
    public void testAddSimulationTag() {
        Simulation simulation = createSimulation("Tests")
        int simulationTagCount = SimulationTag.count()
        Tag tag = new Tag(name: 'tag', tagType: EnumTagType.PARAMETERIZATION).save()

        simulation.setTags([tag] as Set)
        simulation.save()

        assertEquals simulationTagCount + 1, SimulationTag.count()

        simulation.setTags([] as Set)
        simulation.save()

        assertEquals simulationTagCount, SimulationTag.count()


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

    @Test
    void testEquals() {
        Simulation simulation1 = new Simulation('test1')
        Simulation simulation2 = new Simulation('test1')
        assert simulation1.equals(simulation2)
        simulation1.name = 'test2'
        assert !simulation1.equals(simulation2)
        simulation1.id = 4
        simulation2.id = 4
        assert simulation1.equals(simulation2)
        simulation2.id = null
        simulation1.name = 'test1'
        simulation2.modelClass = Object
        simulation1.modelClass = Object
        assert simulation1.equals(simulation2)
        simulation2.modelVersionNumber = new VersionNumber('1.1')
        simulation1.modelVersionNumber = new VersionNumber('1.1')
        assert simulation1.equals(simulation2)
        simulation2.modelClass = String
        assert !simulation1.equals(simulation2)
    }


}
