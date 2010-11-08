package org.pillarone.riskanalytics.core.parameter.comment

import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.user.Person

class CommentDAO {

    ParameterizationDAO parameterization
    String path
    int periodIndex
    Date timeStamp
    String comment
    Person user

    static belongsTo = ParameterizationDAO

    static hasMany = [tags: CommentTag]

    static constraints = {
        user(nullable: true)
        comment(size: 1..4080)
    }

    String toString() {
        "$path P$periodIndex: $comment"
    }
}
