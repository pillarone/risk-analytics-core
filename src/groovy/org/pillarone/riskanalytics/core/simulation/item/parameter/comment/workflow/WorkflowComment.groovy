package org.pillarone.riskanalytics.core.simulation.item.parameter.comment.workflow

import org.pillarone.riskanalytics.core.simulation.item.parameter.comment.Comment
import org.pillarone.riskanalytics.core.parameter.comment.workflow.IssueStatus
import org.pillarone.riskanalytics.core.parameter.comment.workflow.WorkflowCommentDAO
import org.pillarone.riskanalytics.core.parameter.comment.Tag
import org.pillarone.riskanalytics.core.parameter.comment.CommentDAO


class WorkflowComment extends Comment {

    protected IssueStatus status

    public WorkflowComment(WorkflowCommentDAO dao) {
        path = dao.path
        period = dao.periodIndex
        lastChange = dao.timeStamp
        user = dao.user
        comment = dao.comment
        status = dao.status
    }

    public WorkflowComment(String path, int period) {
        super(path, period)
        status = IssueStatus.OPEN
    }

    void addTag(Tag tag) {
        throw new UnsupportedOperationException("Workflow comments do not support tags.")
    }

    void applyToDomainObject(WorkflowCommentDAO dao) {
        dao.path = path
        dao.comment = comment
        dao.timeStamp = lastChange
        dao.user = user
        dao.status = status
    }

    void resolve() {
        if (status == IssueStatus.OPEN) {
            status = IssueStatus.RESOLVED
            updated = true
            updateChangeInfo()
        } else {
            throw new IllegalStateException("Cannot resolve a comment which is ${status.displayName}")
        }
    }

    void close() {
        if (status == IssueStatus.RESOLVED) {
            status = IssueStatus.CLOSED
            updated = true
            updateChangeInfo()
        } else {
            throw new IllegalStateException("Cannot close a comment which is ${status.displayName}")
        }
    }

    void reopen() {
        if (status == IssueStatus.CLOSED) {
            status = IssueStatus.OPEN
            updated = true
            updateChangeInfo()
        } else {
            throw new IllegalStateException("Cannot close a comment which is ${status.displayName}")
        }
    }

    public WorkflowComment clone() {
        return (WorkflowComment) super.clone()
    }

    IssueStatus getStatus() {
        status
    }

}
