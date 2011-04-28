package org.pillarone.riskanalytics.core.parameter.comment

import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.persistence.DateTimeMillisUserType
import org.pillarone.riskanalytics.core.user.Person

class CommentDAO {
    String path
    int periodIndex
    DateTime timeStamp
    String comment
    Person user

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
