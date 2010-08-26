package org.pillarone.riskanalytics.core.parameter.comment

import org.pillarone.riskanalytics.core.user.Person
import org.pillarone.riskanalytics.core.ParameterizationDAO


class CommentDAO {

    ParameterizationDAO parameterization
    String path
    int periodIndex
    Date timeStamp
    String comment
    Person user

    static belongsTo = ParameterizationDAO

    String toString() {
        "$path P$periodIndex: $comment"
    }
}
