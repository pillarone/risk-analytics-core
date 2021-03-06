package org.pillarone.riskanalytics.core.simulation.item

import org.junit.Before
import org.junit.Test
import org.pillarone.riskanalytics.core.ResourceDAO
import org.pillarone.riskanalytics.core.components.ResourceRegistry
import org.pillarone.riskanalytics.core.example.component.ExampleResource
import org.pillarone.riskanalytics.core.parameter.Parameter
import org.pillarone.riskanalytics.core.parameter.comment.CommentTag
import org.pillarone.riskanalytics.core.parameter.comment.ResourceCommentDAO
import org.pillarone.riskanalytics.core.parameter.comment.ResourceTag
import org.pillarone.riskanalytics.core.parameter.comment.Tag
import org.pillarone.riskanalytics.core.parameter.comment.workflow.WorkflowResourceCommentDAO
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolderFactory
import org.pillarone.riskanalytics.core.simulation.item.parameter.comment.Comment
import org.pillarone.riskanalytics.core.simulation.item.parameter.comment.workflow.WorkflowComment

import static org.junit.Assert.*

class ResourceTests {

    @Before
    void setUp() throws Exception {
        ResourceRegistry.clear()
    }

    @Test
    void testSaveLoad() {
        Resource resource = new Resource("resource", ExampleResource)
        resource.addParameter(ParameterHolderFactory.getHolder("path", 0, "param"))
        resource.addParameter(ParameterHolderFactory.getHolder("path2", 0, 100))
        Comment comment = new Comment("path", 0)
        comment.addTag(Tag.list()[0])
        comment.text = "myComment"
        resource.addComment(comment)
        WorkflowComment workflowComment = new WorkflowComment("path", 0)
        workflowComment.text = "myWorkflowComment"
        resource.addComment(workflowComment)
        resource.tags << Tag.list()[0]
        resource.save()

        assertEquals(1, ResourceDAO.count())
        assertEquals(2, Parameter.count())
        assertEquals(1, ResourceCommentDAO.count())
        assertEquals(1, WorkflowResourceCommentDAO.count())
        assertEquals(1, CommentTag.count())
        assertEquals(1, ResourceTag.count())

        resource = new Resource("resource", ExampleResource)
        resource.load(false)
        assertEquals(0, resource.parameterHolders.size())
        resource.load(true)
        assertEquals(2, resource.comments.size())
        assertEquals(1, resource.tags.size())
        assertEquals(2, resource.parameterHolders.size())

        resource.delete()

        assertEquals(0, ResourceDAO.count())
        assertEquals(0, Parameter.count())
        assertEquals(0, ResourceCommentDAO.count())
        assertEquals(0, WorkflowResourceCommentDAO.count())
        assertEquals(0, CommentTag.count())
        assertEquals(0, ResourceTag.count())
    }

    @Test
    void testGetInstance() {
        Resource resource = new Resource("myResource", ExampleResource)
        resource.addParameter(ParameterHolderFactory.getHolder("parmInteger", 0, 99))
        resource.addParameter(ParameterHolderFactory.getHolder("parmString", 0, "String"))
        resource.save()

        resource.load()

        ExampleResource exampleResource = resource.resourceInstance.resource
        assertEquals(99, exampleResource.parmInteger)
        assertEquals("String", exampleResource.parmString)
    }

    @Test
    void testEquals(){
        Resource resource1 = new Resource('test', Resource)
        Resource resource2 = new Resource('test', Resource)
        resource1.versionNumber = new VersionNumber('1.1')
        resource2.versionNumber = new VersionNumber('1.1')
        assert resource1.equals(resource2)
        resource1.id = 1
        resource2.id = 2
        assert !resource1.equals(resource2)
        resource2.id = null
        resource2.name = 'test2'
        assert !resource1.equals(resource2)
        resource2.name = 'test'
        resource2.versionNumber = new VersionNumber('1.2')
        assert !resource1.equals(resource2)
    }
}
