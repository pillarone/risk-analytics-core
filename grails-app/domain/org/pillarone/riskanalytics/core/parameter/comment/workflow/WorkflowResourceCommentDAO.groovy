package org.pillarone.riskanalytics.core.parameter.comment.workflow

import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.ResourceDAO
import org.pillarone.riskanalytics.core.persistence.DateTimeMillisUserType
import org.pillarone.riskanalytics.core.user.Person
import org.pillarone.riskanalytics.core.util.DatabaseUtils

class WorkflowResourceCommentDAO {

    ResourceDAO resource
    String path
    DateTime timeStamp
    String comment
    Person user
    IssueStatus status

    static belongsTo = ResourceDAO

    static constraints = {
        user(nullable: true)
    }

    String toString() {
        "$path $comment"
    }

    static mapping = {
        timeStamp type: DateTimeMillisUserType
        if (DatabaseUtils.isOracleDatabase()) {
            comment(column: "comment_value")
        }
    }
}
