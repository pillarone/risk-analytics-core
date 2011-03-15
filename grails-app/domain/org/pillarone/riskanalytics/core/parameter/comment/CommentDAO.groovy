package org.pillarone.riskanalytics.core.parameter.comment

import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.user.Person
import org.pillarone.riskanalytics.core.persistence.DateTimeMillisUserType
import org.joda.time.DateTime

class CommentDAO {

    ParameterizationDAO parameterization
    String path
    int periodIndex
    DateTime timeStamp
    String comment
    Person user

    static belongsTo = ParameterizationDAO

    static hasMany = [tags: CommentTag]

    static constraints = {
        user(nullable: true)
        comment(size: 1..4080)
    }

    static mapping = {
        timeStamp type: DateTimeMillisUserType
    }

    String toString() {
        "$path P$periodIndex: $comment"
    }
}
