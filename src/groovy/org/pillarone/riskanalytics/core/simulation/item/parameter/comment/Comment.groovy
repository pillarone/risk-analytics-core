package org.pillarone.riskanalytics.core.simulation.item.parameter.comment

import org.pillarone.riskanalytics.core.parameter.comment.CommentDAO
import org.pillarone.riskanalytics.core.parameter.comment.Tag
import org.pillarone.riskanalytics.core.user.Person
import org.pillarone.riskanalytics.core.user.UserManagement
import org.pillarone.riskanalytics.core.parameter.comment.CommentTag

class Comment {

    private String path
    private int period
    private Date lastChange
    private Person user
    private String comment
    private Set<Tag> tags = new HashSet()

    boolean added = false
    boolean updated = false
    boolean deleted = false

    public Comment(CommentDAO commentDAO) {
        path = commentDAO.path
        period = commentDAO.periodIndex
        lastChange = commentDAO.timeStamp
        user = commentDAO.user
        comment = commentDAO.comment
        if (commentDAO.tags?.size() > 0) {
            tags.addAll(commentDAO.tags?.tag)
        }
    }

    public Comment(String path, int period) {
        this.path = path
        this.period = period
        updateChangeInfo()
        added = true
    }

    public List<Tag> getTags() {
        return tags.toList()
    }

    public String getText() {
        return comment
    }

    void setText(String newText) {
        comment = newText
        updated = true
        updateChangeInfo()
    }

    void addTag(Tag tag) {
        tags << tag
        updated = true
        updateChangeInfo()
    }

    void removeTag(Tag tag) {
        tags.remove(tag)
        updated = true
        updateChangeInfo()
    }

    private void updateChangeInfo() {
        user = UserManagement.getCurrentUser()
        lastChange = new Date()
    }

    void applyToDomainObject(CommentDAO dao) {
        dao.path = path
        dao.comment = comment
        dao.timeStamp = lastChange
        dao.user = user
        for (Tag tag in tags) {
            if (!dao.tags*.tag?.contains(tag)) {
                dao.addToTags(new CommentTag(tag: tag))
            }
        }

        List tagsToRemove = []
        for (CommentTag tag in dao.tags) {
            if (!tags.contains(tag.tag)) {
                tagsToRemove << tag
            }
        }
        for (CommentTag tag in tagsToRemove) {
            dao.removeFromTags(tag)
        }
    }
}
