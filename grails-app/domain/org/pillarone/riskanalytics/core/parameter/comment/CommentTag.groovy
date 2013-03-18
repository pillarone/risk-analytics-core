package org.pillarone.riskanalytics.core.parameter.comment


class CommentTag {

    CommentDAO comment
    Tag tag

    static belongsTo = CommentDAO

    String toString() {
        tag
    }
}
