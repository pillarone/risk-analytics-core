package org.pillarone.riskanalytics.core.simulation.item.parameter.comment

import org.pillarone.riskanalytics.core.parameter.comment.CommentDAO
import org.pillarone.riskanalytics.core.parameter.comment.ResultCommentDAO

/**
 * @author fouad.jaada@intuitive-collaboration.com
 */
class FunctionComment extends Comment {

    String function

    public FunctionComment(ResultCommentDAO commentDAO) {
        super(commentDAO)
        function = commentDAO.function
    }

    public FunctionComment(CommentDAO commentDAO) {
        super(commentDAO)
    }

    public FunctionComment(String path, int period, String function) {
        super(path, period)
        this.function = function
    }

    public FunctionComment(Map commentMap) {
        this(commentMap['path'], commentMap['period'], commentMap['function'])
    }

    void applyToDomainObject(CommentDAO dao) {
        super.applyToDomainObject(dao)
        dao.function = function
    }

    public FunctionComment clone() {
        FunctionComment clone = (FunctionComment) super.clone()
        clone.function = function
        return clone
    }


}
