package org.pillarone.riskanalytics.core.parameter.comment

class CommentFileDAO {
    String name
    byte[] content
    static belongsTo = CommentDAO

    static constraints = {
        content(maxSize: 16777215)
    }

    @Override
    String toString() {
        name
    }
}
