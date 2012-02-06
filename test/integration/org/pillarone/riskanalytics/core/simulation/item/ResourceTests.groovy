package org.pillarone.riskanalytics.core.simulation.item

import org.pillarone.riskanalytics.core.components.ResourceComponent
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolderFactory
import org.pillarone.riskanalytics.core.ResourceDAO
import org.pillarone.riskanalytics.core.parameter.Parameter
import org.pillarone.riskanalytics.core.simulation.item.parameter.comment.Comment
import org.pillarone.riskanalytics.core.parameter.comment.ResourceCommentDAO
import org.pillarone.riskanalytics.core.parameter.comment.Tag
import org.pillarone.riskanalytics.core.parameter.comment.CommentTag
import org.pillarone.riskanalytics.core.parameter.comment.ResourceTag
import org.pillarone.riskanalytics.core.simulation.item.parameter.comment.workflow.WorkflowComment
import org.pillarone.riskanalytics.core.parameter.comment.workflow.WorkflowResourceCommentDAO


class ResourceTests extends GroovyTestCase {

    void testSaveLoad() {
        Resource resource = new Resource("resource", ResourceComponent)
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

        resource = new Resource("resource", ResourceComponent)
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
}
