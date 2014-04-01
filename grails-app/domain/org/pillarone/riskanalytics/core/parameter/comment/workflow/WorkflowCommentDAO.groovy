package org.pillarone.riskanalytics.core.parameter.comment.workflow

import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.parameter.comment.CommentFileDAO
import org.pillarone.riskanalytics.core.persistence.DateTimeMillisUserType
import org.pillarone.riskanalytics.core.user.Person
import org.pillarone.riskanalytics.core.util.DatabaseUtils

class WorkflowCommentDAO {

    ParameterizationDAO parameterization
    String path
    int periodIndex
    DateTime timeStamp
    String comment
    Person user
    IssueStatus status

    static belongsTo = ParameterizationDAO
    static hasMany = [commentFile: CommentFileDAO]


    static constraints = {
        user(nullable: true)
    }

    String toString() {
        "$path P$periodIndex: $comment"
    }

    static mapping = {
        timeStamp type: DateTimeMillisUserType
        if (DatabaseUtils.isOracleDatabase()) {
            comment(column: 'comment_value')
        }
    }
}
