package org.pillarone.riskanalytics.core.parameter.comment

class CommentFileDAO {
    String name
    byte[] content
    static belongsTo = CommentDAO

    @Override
    String toString() {
        name
    }
}
