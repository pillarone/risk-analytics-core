package org.pillarone.riskanalytics.core.simulation.item.parameter.comment

import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.parameter.comment.CommentDAO
import org.pillarone.riskanalytics.core.parameter.comment.CommentTag
import org.pillarone.riskanalytics.core.parameter.comment.Tag
import org.pillarone.riskanalytics.core.user.Person
import org.pillarone.riskanalytics.core.user.UserManagement
import org.pillarone.riskanalytics.core.util.GroovyUtils

class Comment implements Cloneable {

    String path
    int period
    DateTime lastChange
    Person user
    protected String comment
    private Set<Tag> tags = new HashSet()
    Set<String> files = new HashSet<String>()

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
        if (commentDAO.files) {
            files.addAll(commentDAO.files.split(","))
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
        files = commentMap['files']
        lastChange = commentMap['lastChange']
    }

    public List<Tag> getTags() {
        return tags.toList()
    }

    public void addFile(String file) {
        files.add(file)
    }

    public void removeFile(String file) {
        files.remove(file)
    }

    public void clearFiles() {
        files.clear()
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
        lastChange = new DateTime()
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
        dao.files = files ? files.join(",") : ""
    }

    public String toConfigObject() {
        //end line character
        char c = (char) 92
        StringBuilder sb = new StringBuilder("\"\"[")
        String newComment = replaceCharacters(comment)
        sb.append("path:'${path}', period:${period}, lastChange:new org.joda.time.DateTime(${lastChange.millis}),user:null, comment: ${c}\"${c}\"${c}\"${newComment}${c}\"${c}\"${c}\"")
        if (tags && !tags.isEmpty()) sb.append(", " + GroovyUtils.toString("tags", tags*.name))
        if (files && !files.isEmpty()) sb.append(", " + GroovyUtils.toString("files", files as List))
        sb.append("]\"\"")
        return sb.toString()
    }

    private String replaceCharacters(String comment) {
        char c = (char) 92
        //replace all occurrences end line Character(\n) in comment  with html code &#92;
        String newComment = comment.replace("${c}", "&#92;")
        //replace quote character by html code
        newComment = newComment.replace('"', '&rdquo;')
        return newComment
    }

    public Comment clone() {
        Comment clone = new Comment(path, period)
        clone.user = user
        clone.comment = comment
        clone.lastChange = (DateTime) new DateTime(lastChange.millis)
        clone.tags = (Set) tags.clone()
        clone.files = (Set) files.clone()

        clone.added = false
        clone.updated = false
        clone.deleted = false
        return clone;
    }


}
