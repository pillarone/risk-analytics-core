package org.pillarone.riskanalytics.core.parameter.comment

import org.pillarone.riskanalytics.core.ResourceDAO


class ResourceCommentDAO extends CommentDAO {

    ResourceDAO resourceDAO

    static belongsTo = ResourceDAO
}
