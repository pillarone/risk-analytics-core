package org.pillarone.riskanalytics.core.simulation.item

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.pillarone.riskanalytics.core.FileConstants
import org.pillarone.riskanalytics.core.parameter.comment.CommentDAO
import org.pillarone.riskanalytics.core.simulation.item.parameter.comment.Comment

/**
 * CommentableItem is a modellingItem and will be extended by parameterization
 * and simulation by using the functionality  of create, edit and delete a comment
 * implemented in this class
 * @author fouad.jaada@intuitive-collaboration.com
 */
abstract class CommentableItem extends ModellingItem {

    List<Comment> comments

    @CompileStatic
    public CommentableItem(String name) {
        super(name)
        comments = []
    }

    @CompileStatic
    void addComment(Comment comment) {
        comments << comment
        comment.added = true
        notifyItemChanged()
    }

    @CompileStatic
    void removeComment(Comment comment) {
        if (comment.added) {
            comments.remove(comment)
            comment.deleted = true
            return
        }
        comment.deleted = true
        comment.updated = false
        notifyItemChanged()
    }


    @TypeChecked
    protected void saveComments(def dao) {
        Iterator<Comment> iterator = comments.iterator()
        while (iterator.hasNext()) {
            Comment comment = iterator.next()
            if (comment.added) {
                commentAdded(dao, comment)
            } else if (comment.updated) {
                commentUpdated(dao, comment)
            } else if (comment.deleted) {
                if (commentDeleted(dao, comment)) {
                    iterator.remove()
                }
            }
        }
    }

    @CompileStatic
    public boolean commentHasChanged() {
        for (Comment comment: comments) {
            if (comment.added || comment.updated || comment.deleted)
                return true
        }
        return false
    }


    protected void commentAdded(def dao, Comment comment) {
        CommentDAO commentDAO = getItemCommentDAO(dao)
        comment.applyToDomainObject(commentDAO)
        dao.addToComments(commentDAO)
        comment.added = false
    }


    protected void commentUpdated(def dao, Comment comment) {
        CommentDAO commentDAO = dao.comments.find { it.path == comment.path && it.periodIndex == comment.period }
        if (commentDAO) {
            comment.applyToDomainObject(commentDAO)
            comment.updated = false
        }
    }

    protected boolean commentDeleted(def dao, Comment comment) {
        CommentDAO commentDAO = dao.comments.find { it.path == comment.path && it.periodIndex == comment.period }
        if (commentDAO) {
            dao.removeFromComments(commentDAO)
            commentDAO.delete()
            return true
        }
        return false
    }

    CommentDAO getItemCommentDAO(def dao) {
        return null
    }


}
