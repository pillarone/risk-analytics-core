package org.pillarone.riskanalytics.core.parameter.comment

import org.pillarone.riskanalytics.core.ParameterizationDAO

class ParameterizationCommentDAO extends CommentDAO {

    ParameterizationDAO parameterization

    static belongsTo = ParameterizationDAO


}
