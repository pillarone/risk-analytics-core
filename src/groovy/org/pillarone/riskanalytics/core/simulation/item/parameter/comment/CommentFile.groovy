package org.pillarone.riskanalytics.core.simulation.item.parameter.comment

import groovy.transform.CompileStatic
import org.pillarone.riskanalytics.core.parameter.comment.CommentFileDAO

class CommentFile {
    final String filename
    final File file
    final Long persistedId

    CommentFile(String filename, Long persistedId) {
        this.filename = filename
        this.persistedId = persistedId
    }

    CommentFile(String filename, File file) {
        this.filename = filename
        this.file = file
    }

    byte[] getContent() {
        if (persistedId) {
            return CommentFileDAO.get(persistedId).content
        } else {
            return file.bytes
        }
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        CommentFile that = (CommentFile) o

        if (file != that.file) return false
        if (filename != that.filename) return false
        if (persistedId != that.persistedId) return false

        return true
    }

    int hashCode() {
        int result
        result = (filename != null ? filename.hashCode() : 0)
        result = 31 * result + (file != null ? file.hashCode() : 0)
        result = 31 * result + (persistedId != null ? persistedId.hashCode() : 0)
        return result
    }
}
