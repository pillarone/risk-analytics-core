package org.pillarone.riskanalytics.core.simulation.item

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

    public CommentableItem(String name) {
        super(name)
        comments = []
    }

    void addComment(Comment comment) {
        comments << comment
        comment.added = true
        notifyItemChanged()
    }

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
            deleteCommentFiles(commentDAO)
            return true
        }
        return false
    }

    private void deleteCommentFiles(CommentDAO commentDAO) {
        if (!commentDAO.files) return
        commentDAO.files.split(",").each {String fileName ->
            try {
                File file = new File(FileConstants.COMMENT_FILE_DIRECTORY + File.separator + fileName)
                if (file.exists()) {
                    file.delete()
                    LOG.info "comment file  $file deleted"
                }
            } catch (Exception ex) {
                LOG.error "error occured during delete a file ${fileName} : ${ex}"
            }
        }

    }


    CommentDAO getItemCommentDAO(def dao) {
        return null
    }


}
