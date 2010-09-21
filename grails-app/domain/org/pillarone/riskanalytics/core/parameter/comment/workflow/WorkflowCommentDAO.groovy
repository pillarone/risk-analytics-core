package org.pillarone.riskanalytics.core.parameter.comment.workflow

import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.user.Person


class WorkflowCommentDAO {

    ParameterizationDAO parameterization
    String path
    int periodIndex
    Date timeStamp
    String comment
    Person user
    IssueStatus status

    static belongsTo = ParameterizationDAO

    static constraints = {
        user(nullable: true)
    }

    String toString() {
        "$path P$periodIndex: $comment"
    }
}
