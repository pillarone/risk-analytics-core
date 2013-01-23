package org.pillarone.riskanalytics.core.simulation.item

import models.core.CoreModel
import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.example.model.EmptyModel
import org.pillarone.riskanalytics.core.example.parameter.ExampleParameterObject
import org.pillarone.riskanalytics.core.example.parameter.ExampleParameterObjectClassifier
import org.pillarone.riskanalytics.core.fileimport.ParameterizationImportService
import org.pillarone.riskanalytics.core.parameter.Parameter
import org.pillarone.riskanalytics.core.parameter.StringParameter
import org.pillarone.riskanalytics.core.parameter.comment.CommentDAO
import org.pillarone.riskanalytics.core.parameter.comment.ParameterizationCommentDAO
import org.pillarone.riskanalytics.core.parameter.comment.Tag
import org.pillarone.riskanalytics.core.parameter.comment.workflow.WorkflowCommentDAO
import org.pillarone.riskanalytics.core.parameterization.validation.TestValidationService
import org.pillarone.riskanalytics.core.parameterization.validation.ValidatorRegistry
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolder
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolderFactory
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterObjectParameterHolder
import org.pillarone.riskanalytics.core.simulation.item.parameter.StringParameterHolder
import org.pillarone.riskanalytics.core.simulation.item.parameter.comment.Comment
import org.pillarone.riskanalytics.core.simulation.item.parameter.comment.EnumTagType
import org.pillarone.riskanalytics.core.simulation.item.parameter.comment.workflow.WorkflowComment
import org.pillarone.riskanalytics.core.workflow.Status

class ParameterizationTests extends GroovyTestCase {

    void setUp() {
        if (!ValidatorRegistry.contains(TestValidationService.class)) {
            ValidatorRegistry.addValidator(new TestValidationService())
        }
    }

    void testLoad() {

        Parameterization unknownParameterization = new Parameterization("unknown name")
        assertEquals "Name not set", "unknown name", unknownParameterization.name
        assertNull unknownParameterization.modelClass
        unknownParameterization.load()
        assertNull unknownParameterization.modelClass
        assertEquals "Name not set after load", "unknown name", unknownParameterization.name

        ParameterizationDAO dao = createDao(EmptyModel, "myDao")
        if (!dao.save()) {
            dao.errors.each {
                println it
            }
        }

        Parameterization parameterization = new Parameterization("myDao")
        assertEquals "wrong name", "myDao", parameterization.name
        assertNull parameterization.modelClass

        parameterization.load()

        assertNotNull parameterization.dao
        assertNotNull parameterization.modelClass
        assertNotNull parameterization.tags
        assertSame "wrong model class", EmptyModel, parameterization.modelClass
    }

    public void testLoadDoesOverwriteChanges() {
        String daoName = "myOtherDao"

        ParameterizationDAO dao = createDao(EmptyModel, daoName)
        // todo (msh) : insert parameter
        if (!dao.save()) {
            dao.errors.each {
                println it
            }
        }


        Parameterization parameterization = new Parameterization("myOtherDao")
        parameterization.load()
        // todo (msh) : modify parameter
        parameterization.load()

    }


    public void testModelClass() {
        Parameterization parameterization = new Parameterization("p")
        assertNull parameterization.getModelClass()

        parameterization.setModelClass(EmptyModel)
        assertSame EmptyModel, parameterization.getModelClass()

        parameterization.modelClass = CoreModel
        assertSame CoreModel, parameterization.modelClass
    }


    public void testSave() {
        Parameterization parameterization = new Parameterization("newParams")
        parameterization.modelClass = EmptyModel
        parameterization.periodCount = 1
        parameterization.save()

        assertTrue parameterization.valid

        Parameterization savedParameterization = new Parameterization("newParams")
        savedParameterization.load()
        assertEquals parameterization.name, savedParameterization.name
        assertEquals EmptyModel, parameterization.modelClass
        assertEquals 1, parameterization.periodCount
        assertEquals '1', parameterization.versionNumber.toString()

        savedParameterization.modelClass = CoreModel

        savedParameterization.save()

        Parameterization reloadedParameterization = new Parameterization("newParams")
        reloadedParameterization.load()

        assertEquals "modelClass not changed", CoreModel, reloadedParameterization.modelClass
        assertNotNull parameterization.tags
    }


    void testAddRemoveParameter() {
        Parameterization parameterization = new Parameterization("newParams")
        parameterization.periodCount = 1
        parameterization.modelClass = EmptyModel

        int initialCount = Parameter.count()

        StringParameterHolder newHolder = new StringParameterHolder(new StringParameter(path: "path", periodIndex: 0, parameterValue: "value"))
        parameterization.addParameter(newHolder)
        parameterization.removeParameter(newHolder)

        parameterization.save()

        assertEquals 0, parameterization.parameters.size()
        assertEquals initialCount, Parameter.count()

        parameterization.addParameter(newHolder)

        parameterization.save()

        assertEquals 1, parameterization.parameters.size()
        assertEquals initialCount + 1, Parameter.count()

        parameterization.removeParameter(newHolder)

        parameterization.save()

        assertEquals 0, parameterization.parameters.size()
        assertEquals initialCount, Parameter.count()
    }


    void testAddRemoveComment() {
        Parameterization parameterization = new Parameterization("newParams")
        parameterization.periodCount = 1
        parameterization.modelClass = EmptyModel

        int initialCount = CommentDAO.count()

        Comment newComment = new Comment("path", 0)
        newComment.text = "text"
        newComment.addFile("file1")
        newComment.addFile("file1")
        assertTrue newComment.getFiles().size() == 1

        parameterization.addComment(newComment)
        parameterization.removeComment(newComment)

        parameterization.save()

        assertEquals 0, parameterization.comments.size()
        assertEquals initialCount, ParameterizationCommentDAO.count()

        parameterization.addComment(newComment)

        parameterization.save()

        assertEquals 1, parameterization.comments.size()
        assertEquals 1, parameterization.getSize(ParameterizationCommentDAO)
        assertEquals initialCount + 1, ParameterizationCommentDAO.count()

        parameterization.removeComment(newComment)

        parameterization.save()

        assertEquals 0, parameterization.comments.size()
        assertEquals initialCount, ParameterizationCommentDAO.count()
    }


    void testAddRemoveIssue() {
        Parameterization parameterization = new Parameterization("newParams")
        parameterization.periodCount = 1
        parameterization.modelClass = EmptyModel

        int initialCount = WorkflowCommentDAO.count()

        WorkflowComment newComment = new WorkflowComment("path", 0)
        newComment.text = "text"

        parameterization.addComment(newComment)
        parameterization.removeComment(newComment)

        parameterization.save()

        assertEquals 0, parameterization.comments.size()
        assertEquals initialCount, WorkflowCommentDAO.count()

        parameterization.addComment(newComment)

        parameterization.save()

        assertEquals 1, parameterization.comments.size()
        assertEquals initialCount + 1, WorkflowCommentDAO.count()

        parameterization.removeComment(newComment)

        parameterization.save()

        assertEquals 0, parameterization.comments.size()
        assertEquals initialCount, WorkflowCommentDAO.count()
    }

    void testSimpleParameterUpdate() {
        Parameterization parameterization = new Parameterization("testSimpleParameterUpdate")
        parameterization.periodCount = 1
        parameterization.modelClass = EmptyModel

        int initialCount = Parameter.count()

        StringParameterHolder newHolder = new StringParameterHolder(new StringParameter(path: "path", periodIndex: 0, parameterValue: "value"))
        parameterization.addParameter(newHolder)

        parameterization.save()

        assertEquals 1, parameterization.parameters.size()
        assertEquals initialCount + 1, Parameter.count()

        newHolder.value = "newValue"

        parameterization.save()

        assertEquals 1, parameterization.parameters.size()
        assertEquals initialCount + 1, Parameter.count()

        parameterization.load()
        assertEquals 1, parameterization.parameters.size()

        assertEquals "newValue", parameterization.parameters[0].businessObject
    }

    void testSimpleCommentUpdate() {
        Parameterization parameterization = new Parameterization("testSimpleParameterUpdate")
        parameterization.periodCount = 1
        parameterization.modelClass = EmptyModel

        int initialCount = ParameterizationCommentDAO.count()

        Comment comment = new Comment("path", 0)
        comment.text = "text"
        parameterization.addComment(comment)

        parameterization.save()

        assertEquals 1, parameterization.comments.size()
        assertEquals initialCount + 1, ParameterizationCommentDAO.count()

        comment.text = "newValue"

        parameterization.save()

        assertEquals 1, parameterization.comments.size()
        assertEquals initialCount + 1, ParameterizationCommentDAO.count()

        parameterization.load()
        assertEquals 1, parameterization.comments.size()

        assertEquals "newValue", parameterization.comments[0].text
    }

    void testSimpleIssueUpdate() {
        Parameterization parameterization = new Parameterization("testSimpleParameterUpdate")
        parameterization.periodCount = 1
        parameterization.modelClass = EmptyModel

        int initialCount = WorkflowCommentDAO.count()

        WorkflowComment comment = new WorkflowComment("path", 0)
        comment.text = "text"
        parameterization.addComment(comment)

        parameterization.save()

        assertEquals 1, parameterization.comments.size()
        assertEquals initialCount + 1, WorkflowCommentDAO.count()

        comment.text = "newValue"

        parameterization.save()

        assertEquals 1, parameterization.comments.size()
        assertEquals initialCount + 1, WorkflowCommentDAO.count()

        parameterization.load()
        assertEquals 1, parameterization.comments.size()

        assertEquals "newValue", parameterization.comments[0].text
    }

    void testPMO_1007() {
        Parameterization parameterization = new Parameterization("testSimpleParameterUpdate")
        parameterization.periodCount = 1
        parameterization.modelClass = EmptyModel


        StringParameterHolder newHolder = new StringParameterHolder(new StringParameter(path: "path", periodIndex: 0, parameterValue: "value"))
        parameterization.addParameter(newHolder)

        assertTrue parameterization.getParameters("path").size() > 0
        newHolder.removed = true
        assertFalse parameterization.getParameters("path").size() > 0
    }

    void testSaveOfParameter() {
        Parameterization parameterization = new Parameterization("newParams")
        parameterization.modelClass = EmptyModel
        StringParameter parameter = new StringParameter(path: "path", parameterValue: "value", periodIndex: 0)
        assertNull parameter.id
        parameterization.periodCount = 1
        parameterization.addParameter(new StringParameterHolder(parameter))

        parameterization.save()
        assertNotNull parameterization.id
    }


    void testGetParameters() {
        Parameterization parameterization = new Parameterization("newParams")
        parameterization.modelClass = EmptyModel
        parameterization.periodCount = 1
        StringParameterHolder parameterA = new StringParameterHolder(new StringParameter(path: "a", parameterValue: "value"))
        StringParameterHolder parameterB = new StringParameterHolder(new StringParameter(path: "a.b", parameterValue: "value"))

        parameterization.addParameter(parameterA)
        parameterization.addParameter(parameterB)

        parameterization.save()

        List parameters = parameterization.getParameters()
        assertNotNull parameters
        assertEquals 2, parameters.size()
        assertTrue parameters.contains(parameterA)
        assertTrue parameters.contains(parameterB)
    }

    void testGetParametersByPath() {
        Parameterization parameterization = new Parameterization("newParams")
        parameterization.modelClass = EmptyModel
        parameterization.periodCount = 4
        StringParameterHolder parameterD = new StringParameterHolder(new StringParameter(path: "a", parameterValue: "value", periodIndex: 3))
        StringParameterHolder parameterC = new StringParameterHolder(new StringParameter(path: "a", parameterValue: "value", periodIndex: 2))
        StringParameterHolder parameterB = new StringParameterHolder(new StringParameter(path: "a", parameterValue: "value", periodIndex: 1))
        StringParameterHolder parameterA = new StringParameterHolder(new StringParameter(path: "a", parameterValue: "value", periodIndex: 0))

        parameterization.addParameter(parameterA)
        parameterization.addParameter(parameterB)
        parameterization.addParameter(parameterC)
        parameterization.addParameter(parameterD)

        parameterization.save()


        List parameters = parameterization.getParameters('a')
        assertNotNull parameters
        assertEquals 4, parameters.size()
        assertTrue parameters.contains(parameterA)
        assertTrue parameters.contains(parameterB)
        assertTrue parameters.contains(parameterC)
        assertTrue parameters.contains(parameterD)

        assertTrue "paramaters must be sorted by period", parameters[0].periodIndex < parameters[1].periodIndex
        assertTrue "paramaters must be sorted by period", parameters[1].periodIndex < parameters[2].periodIndex
        assertTrue "paramaters must be sorted by period", parameters[2].periodIndex < parameters[3].periodIndex
    }

    void testEquals() {

        Parameterization p1 = new Parameterization('Name')
        p1.modelClass = EmptyModel

        Parameterization p2 = new Parameterization('Name')
        p2.modelClass = EmptyModel

        assertTrue p1.equals(p2)
        assertTrue p1.hashCode().equals(p2.hashCode())

        //ART-113
        Simulation simulation = new Simulation('Name')
        simulation.modelClass = EmptyModel
        assertFalse p1.equals(simulation)
    }

    void testToConfigObject() {
        new ParameterizationImportService().compareFilesAndWriteToDB(['CoreParameters'])

        Parameterization parameterization = new Parameterization('CoreParameters')

        ConfigObject configObject = parameterization.toConfigObject()


        assertEquals 8, configObject.size()
        assertTrue configObject.containsKey("applicationVersion")
        assertTrue configObject.containsKey("periodLabels")
        assertEquals CoreModel, configObject.model
        assertEquals 1, configObject.periodCount
        assertEquals 'CoreParameters', configObject.displayName

        ConfigObject components = configObject.components
        assertEquals 2, components.size()

        def distribution = components.exampleInputOutputComponent.parmParameterObject[0]
        assertTrue distribution instanceof ExampleParameterObject

        parameterization.periodLabels = ["Q1"]
        configObject = parameterization.toConfigObject()
        assertEquals 8, configObject.size()
        assertEquals "periodLabels", ["Q1"], configObject.periodLabels

        parameterization.tags << new Tag(name: "MyTag", tagType: EnumTagType.PARAMETERIZATION)
        parameterization.tags << new Tag(name: Tag.LOCKED_TAG, tagType: EnumTagType.PARAMETERIZATION)

        //do not export locked tag
        configObject = parameterization.toConfigObject()
        assertEquals(1, configObject.tags.size())
        assertEquals("MyTag", configObject.tags[0])

    }

    void testValidation() {
        Parameterization parameterization = new Parameterization("testValidationList")
        assertNull parameterization.validationErrors
        parameterization.validate()
        assertNotNull parameterization.validationErrors
        assertEquals 0, parameterization.validationErrors.size()
        assertTrue parameterization.valid

        parameterization.addParameter(new StringParameterHolder("path", 0, "VALID"))
        parameterization.validate()

        assertEquals 0, parameterization.validationErrors.size()
        assertTrue parameterization.valid

        parameterization.addParameter(new StringParameterHolder("path", 0, "INVALID"))
        parameterization.validate()

        assertEquals 1, parameterization.validationErrors.size()
        assertFalse parameterization.valid

        StringParameterHolder holder = new StringParameterHolder("path", 0, "INVALID")
        holder.removed = true
        parameterization.addParameter(holder)
        parameterization.validate()

        // do not validate removed params
        assertEquals 1, parameterization.validationErrors.size()
        assertFalse parameterization.valid
    }

    void testIsLoaded() {
        Parameterization parameterization = new Parameterization("testIsLoaded")
        parameterization.modelClass = EmptyModel
        parameterization.periodCount = 1
        parameterization.save()

        parameterization.load(false)
        assertFalse(parameterization.isLoaded())

        parameterization.load()
        assertTrue(parameterization.isLoaded())

    }

    void testIsEditable() {
        Parameterization parameterization = new Parameterization("newParams")
        parameterization.modelClass = EmptyModel
        parameterization.periodCount = 1
        parameterization.save()

        assertEquals Status.NONE, parameterization.status

        assertTrue parameterization.isEditable()

        parameterization.status = Status.DATA_ENTRY
        assertTrue parameterization.isEditable()

        parameterization.status = Status.REJECTED
        assertFalse parameterization.isEditable()

        parameterization.status = Status.IN_REVIEW
        assertFalse parameterization.isEditable()

        parameterization.status = Status.IN_PRODUCTION
        assertFalse parameterization.isEditable()
    }

    void testSafeReload() {
        Parameterization parameterization = new Parameterization("test", CoreModel)
        final ParameterHolder holder = ParameterHolderFactory.getHolder("testSafeReload", 0, 1)
        parameterization.addParameter(holder)
        parameterization.save()
        parameterization.load()

        Parameter parameter = Parameter.findByPath("testSafeReload")
        parameter.integerValue = 2
        parameter.save()

        parameterization.load()

        assertEquals(1, parameterization.parameterHolders.size())
        assertSame(holder, parameterization.parameterHolders[0])
        assertEquals(2, holder.businessObject)

    }

    void testGetParameterHolder() {
        Parameterization parameterization = new Parameterization("testSafeNestedReload", CoreModel)
        ParameterObjectParameterHolder holder = ParameterHolderFactory.getHolder("component:parmParameter", 0, ExampleParameterObjectClassifier.getStrategy(ExampleParameterObjectClassifier.NESTED_PARAMETER_OBJECT, ExampleParameterObjectClassifier.NESTED_PARAMETER_OBJECT.parameters))
        parameterization.addParameter(holder)

        assertSame(holder, parameterization.getParameterHolder("component:parmParameter", 0))
        assertSame(holder.classifierParameters["nested"], parameterization.getParameterHolder("component:parmParameter:nested", 0))
        assertSame(holder.classifierParameters["nested"].classifierParameters["a"], parameterization.getParameterHolder("component:parmParameter:nested:a", 0))

        assertTrue(parameterization.hasParameterAtPath("component:parmParameter"))
        assertTrue(parameterization.hasParameterAtPath("component:parmParameter",0))
        assertFalse(parameterization.hasParameterAtPath("component:parmParameter",1))

        parameterization.addParameter(ParameterHolderFactory.getHolder("component:parmParameter", 1, ExampleParameterObjectClassifier.getStrategy(ExampleParameterObjectClassifier.NESTED_PARAMETER_OBJECT, ExampleParameterObjectClassifier.NESTED_PARAMETER_OBJECT.parameters)))

        List<ParameterHolder> parameterHolders = parameterization.getParameterHoldersForAllPeriods("component:parmParameter").sort { it.periodIndex }
        assertEquals(2, parameterHolders.size())
    }

    void testSafeNestedReload() {
        Parameterization parameterization = new Parameterization("testSafeNestedReload", CoreModel)
        ParameterObjectParameterHolder holder = ParameterHolderFactory.getHolder("testSafeNestedReload", 0, ExampleParameterObjectClassifier.getStrategy(ExampleParameterObjectClassifier.NESTED_PARAMETER_OBJECT, ExampleParameterObjectClassifier.NESTED_PARAMETER_OBJECT.parameters))
        parameterization.addParameter(holder)

        ParameterHolder veryNestedHolder = holder.classifierParameters["nested"].classifierParameters["a"]

        parameterization.save()
        assertNotNull(parameterization.id)
        parameterization.load()

        assertSame(veryNestedHolder, parameterization.parameterHolders[0].classifierParameters["nested"].classifierParameters["a"])
    }


    private def createDao(Class modelClass, String daoName) {
        ParameterizationDAO dao = new ParameterizationDAO()
        dao.periodCount = 1
        dao.itemVersion = '1'
        dao.name = daoName
        dao.modelClassName = modelClass.name
        dao.status = Status.NONE
        return dao
    }
}
