package org.pillarone.riskanalytics.core.simulation.item.parameter.comment

import org.pillarone.riskanalytics.core.parameter.comment.CommentDAO
import org.pillarone.riskanalytics.core.parameter.comment.CommentTag
import org.pillarone.riskanalytics.core.parameter.comment.Tag
import org.pillarone.riskanalytics.core.user.Person
import org.pillarone.riskanalytics.core.user.UserManagement

class Comment implements Cloneable {

    String path
    int period
    Date lastChange
    Person user
    protected String comment
    private Set<Tag> tags = new HashSet()

    boolean added = false
    boolean updated = false
    boolean deleted = false

    protected Comment() { }

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

    public Comment(Map commentMap) {
        this(commentMap['path'], commentMap['period'])

        comment = commentMap['comment']
        commentMap['tags']?.each {String tagName ->
            Tag tag = Tag.findByName(tagName)
            if (!tag) {
                tag = new Tag(name: tagName)
                tag.save()
            }
            addTag(tag)
        }
        lastChange = commentMap['lastChange']
    }

    public List<Tag> getTags() {
        return tags.toList()
    }

    public void setTags(Set selectedTags) {
        selectedTags.each {Tag tag ->
            if (!tags.contains(tag))
                addTag(tag)
        }
        List tagsToRemove = []
        tags.each {Tag tag ->
            if (!selectedTags.contains(tag))
                tagsToRemove << tag
        }
        if (tagsToRemove.size() > 0)
            tags.removeAll(tagsToRemove)
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

    protected void updateChangeInfo() {
        user = UserManagement.getCurrentUser()
        lastChange = new Date()
    }

    void applyToDomainObject(CommentDAO dao) {
        dao.path = path
        dao.comment = comment
        dao.timeStamp = lastChange
        dao.user = user
        dao.periodIndex = period

        List tagsToRemove = []
        for (CommentTag tag in dao.tags) {
            if (!tags.contains(tag.tag)) {
                tagsToRemove << tag
            }
        }
        for (CommentTag tag in tagsToRemove) {
            dao.removeFromTags(tag)

        }
        tagsToRemove.each {it.delete()}

        for (Tag tag in tags) {
            if (!dao.tags*.tag?.contains(tag)) {
                dao.addToTags(new CommentTag(tag: tag))
            }
        }

    }

    public String toConfigObject() {
        char c = (char) 92
        StringBuilder sb = new StringBuilder("\"\"[")
        //replace all occurrences end line Character(\n) in comment  with html code &#92;
        String newComment = comment.replace("${c}", "&#92;")
        sb.append("path:'${path}', period:${period}, lastChange:new Date(${lastChange.getTime()}),user:null, comment: ${c}\"${c}\"${c}\"${newComment}${c}\"${c}\"${c}\"")
        if (tags && !tags.isEmpty()) {
            sb.append(", tags:([")
            tags.eachWithIndex {Tag tag, int index ->
                sb.append("'" + tag.name + "'")
                if (index != tags.size() - 1)
                    sb.append(",")
            }
            sb.append("] as Set)")
        }
        sb.append("]\"\"")
        return sb.toString()
    }

    public Comment clone() {
        Comment clone = (Comment) super.clone()
        clone.lastChange = (Date) lastChange.clone()
        clone.tags = (Set) tags.clone()

        clone.added = false
        clone.updated = false
        clone.deleted = false
        return clone;
    }


}
