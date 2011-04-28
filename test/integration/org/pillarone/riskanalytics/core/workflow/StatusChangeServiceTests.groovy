package org.pillarone.riskanalytics.core.workflow

import org.pillarone.riskanalytics.core.example.model.EmptyModel
import org.pillarone.riskanalytics.core.parameter.comment.workflow.IssueStatus
import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import org.pillarone.riskanalytics.core.simulation.item.VersionNumber
import org.pillarone.riskanalytics.core.simulation.item.parameter.comment.Comment
import org.pillarone.riskanalytics.core.simulation.item.parameter.comment.workflow.WorkflowComment
import static org.pillarone.riskanalytics.core.workflow.Status.*
import org.pillarone.riskanalytics.core.remoting.impl.RemotingUtils

class StatusChangeServiceTests extends GroovyTestCase {

    StatusChangeService statusChangeService

    void testToDataEntryFromNone() {
        Parameterization parameterization = new Parameterization("name")
        parameterization.modelClass = EmptyModel
        parameterization.periodCount = 0
        parameterization.dealId = 1
        parameterization.save()

        Parameterization newParameterization = statusChangeService.changeStatus(parameterization, DATA_ENTRY)
        assertNotSame newParameterization, parameterization

        assertEquals NONE, parameterization.status
        assertEquals RemotingUtils.transactionService.allTransactions.find {it.dealId == newParameterization.dealId}.name, newParameterization.name

        assertEquals DATA_ENTRY, newParameterization.status
        assertEquals "R1", newParameterization.versionNumber.toString()
    }

    void testToDataEntryFromNoneAlreadyInWorkflow() {
        Parameterization existingParameterization = new Parameterization("name")
        existingParameterization.modelClass = EmptyModel
        existingParameterization.periodCount = 0
        existingParameterization.dealId = 1
        existingParameterization.versionNumber = new VersionNumber("R1")
        existingParameterization.save()

        Parameterization parameterization = new Parameterization("name")
        parameterization.modelClass = EmptyModel
        parameterization.periodCount = 0
        parameterization.dealId = 1
        parameterization.save()

        shouldFail(WorkflowException, { statusChangeService.changeStatus(parameterization, DATA_ENTRY)})
    }

    void testToProduction() {
        Parameterization parameterization = new Parameterization("name")
        parameterization.status = IN_REVIEW
        parameterization.versionNumber = new VersionNumber("R1")
        parameterization.modelClass = EmptyModel
        parameterization.periodCount = 0
        parameterization.save()

        Parameterization newParameterization = statusChangeService.changeStatus(parameterization, IN_PRODUCTION)
        assertSame newParameterization, parameterization

        assertEquals IN_PRODUCTION, parameterization.status
        assertEquals "R1", parameterization.versionNumber.toString()
    }

    void testToInReview() {
        Parameterization parameterization = new Parameterization("name")
        parameterization.status = DATA_ENTRY
        parameterization.versionNumber = new VersionNumber("R1")
        parameterization.modelClass = EmptyModel
        parameterization.periodCount = 0
        parameterization.save()

        Parameterization newParameterization = statusChangeService.changeStatus(parameterization, IN_REVIEW)
        assertSame newParameterization, parameterization

        assertEquals IN_REVIEW, parameterization.status
        assertEquals "R1", parameterization.versionNumber.toString()
    }

    void testToDataEntryFromInReview() {
        Parameterization parameterization = new Parameterization("name")
        parameterization.status = IN_REVIEW
        parameterization.versionNumber = new VersionNumber("R1")
        parameterization.modelClass = EmptyModel
        parameterization.periodCount = 0
        parameterization.save()

        Parameterization newParameterization = statusChangeService.changeStatus(parameterization, DATA_ENTRY)
        assertNotSame newParameterization, parameterization

        assertEquals REJECTED, parameterization.status

        assertEquals DATA_ENTRY, newParameterization.status
        assertEquals "R2", newParameterization.versionNumber.toString()
    }

    void testCopyNonClosedIssues() {
        Parameterization parameterization = new Parameterization("name")
        parameterization.status = IN_REVIEW
        parameterization.versionNumber = new VersionNumber("R1")
        parameterization.modelClass = EmptyModel
        parameterization.periodCount = 0

        Comment comment = new Comment("path", 0)
        comment.text = "text"
        parameterization.addComment(comment)

        WorkflowComment comment2 = new WorkflowComment("path", 0)
        comment2.text = "text"
        parameterization.addComment(comment2)

        WorkflowComment comment3 = new WorkflowComment("path", 0)
        comment3.text = "text"
        comment3.resolve()
        comment3.close()
        parameterization.addComment(comment3)

        parameterization.save()

        Parameterization newParameterization = statusChangeService.changeStatus(parameterization, DATA_ENTRY)
        assertNotSame newParameterization, parameterization

        assertEquals 2, newParameterization.comments.size()
        assertEquals IssueStatus.OPEN, newParameterization.comments.findAll {it instanceof WorkflowComment}[0].status

    }

    void testCommentsToDataEntry() {
        Parameterization parameterization = new Parameterization("name")
        parameterization.status = IN_REVIEW
        parameterization.versionNumber = new VersionNumber("R1")
        parameterization.modelClass = EmptyModel
        parameterization.periodCount = 0

        WorkflowComment comment3 = new WorkflowComment("path", 0)
        comment3.text = "text"
        comment3.resolve()
        comment3.close()
        parameterization.addComment(comment3)

        parameterization.save()

        Parameterization newParameterization = statusChangeService.changeStatus(parameterization, DATA_ENTRY)
        assertNotSame newParameterization, parameterization

        assertEquals 0, newParameterization.comments.size()
        assertEquals DATA_ENTRY, newParameterization.status
    }

    void testCommentsToDataEntryFailed() {
        Parameterization parameterization = new Parameterization("name")
        parameterization.status = IN_REVIEW
        parameterization.versionNumber = new VersionNumber("R1")
        parameterization.modelClass = EmptyModel
        parameterization.periodCount = 0

        WorkflowComment comment3 = new WorkflowComment("path", 0)
        comment3.text = "text"
        comment3.resolve()
        parameterization.addComment(comment3)

        parameterization.save()

        shouldFail(WorkflowException, { statusChangeService.changeStatus(parameterization, DATA_ENTRY) })
    }

    void testCommentsToInReview() {
        Parameterization parameterization = new Parameterization("name")
        parameterization.status = DATA_ENTRY
        parameterization.versionNumber = new VersionNumber("R1")
        parameterization.modelClass = EmptyModel
        parameterization.periodCount = 0

        WorkflowComment comment3 = new WorkflowComment("path", 0)
        comment3.text = "text"
        comment3.resolve()
        parameterization.addComment(comment3)

        parameterization.save()

        Parameterization newParameterization = statusChangeService.changeStatus(parameterization, IN_REVIEW)
        assertSame newParameterization, parameterization

        assertEquals 1, newParameterization.comments.size()
        assertEquals IN_REVIEW, newParameterization.status
    }

    void testCommentsToInReviewFailed() {
        Parameterization parameterization = new Parameterization("name")
        parameterization.status = DATA_ENTRY
        parameterization.versionNumber = new VersionNumber("R1")
        parameterization.modelClass = EmptyModel
        parameterization.periodCount = 0

        WorkflowComment comment3 = new WorkflowComment("path", 0)
        comment3.text = "text"
        parameterization.addComment(comment3)

        parameterization.save()

        shouldFail(WorkflowException, { statusChangeService.changeStatus(parameterization, IN_REVIEW) })
    }

    void testCommentsToInProduction() {
        Parameterization parameterization = new Parameterization("name")
        parameterization.status = IN_REVIEW
        parameterization.versionNumber = new VersionNumber("R1")
        parameterization.modelClass = EmptyModel
        parameterization.periodCount = 0

        WorkflowComment comment3 = new WorkflowComment("path", 0)
        comment3.text = "text"
        comment3.resolve()
        comment3.close()
        parameterization.addComment(comment3)

        parameterization.save()

        Parameterization newParameterization = statusChangeService.changeStatus(parameterization, IN_PRODUCTION)
        assertSame newParameterization, parameterization

        assertEquals 1, newParameterization.comments.size()
        assertEquals IN_PRODUCTION, newParameterization.status
    }

    void testCommentsToInProductionFailed() {
        Parameterization parameterization = new Parameterization("name")
        parameterization.status = IN_REVIEW
        parameterization.versionNumber = new VersionNumber("R1")
        parameterization.modelClass = EmptyModel
        parameterization.periodCount = 0

        WorkflowComment comment3 = new WorkflowComment("path", 0)
        comment3.text = "text"
        parameterization.addComment(comment3)

        parameterization.save()

        shouldFail(WorkflowException, { statusChangeService.changeStatus(parameterization, IN_PRODUCTION) })
    }

    void testNewVersion() {
        Parameterization parameterization = new Parameterization("name")
        parameterization.status = IN_PRODUCTION
        parameterization.versionNumber = new VersionNumber("R1")
        parameterization.dealId = 1
        parameterization.modelClass = EmptyModel
        parameterization.periodCount = 0
        parameterization.save()

        Parameterization newParameterization = statusChangeService.changeStatus(parameterization, DATA_ENTRY)
        assertEquals parameterization.name, newParameterization.name
        assertEquals parameterization.dealId, newParameterization.dealId

        assertEquals "R2", newParameterization.versionNumber.toString()
        assertEquals DATA_ENTRY, newParameterization.status
    }
}
