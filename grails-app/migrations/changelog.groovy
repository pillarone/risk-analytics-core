import org.pillarone.riskanalytics.core.FileConstants
import org.pillarone.riskanalytics.core.parameter.comment.CommentDAO
import org.pillarone.riskanalytics.core.parameter.comment.CommentFileDAO

databaseChangeLog = {
    changeSet(author: "detlef.brendle", id: "migrate-comment-files-to-db") {
        grailsChange {
            change {
                CommentDAO.withNewSession {
                    CommentDAO.list().each { CommentDAO commentDao ->
                        if (commentDao.files) {
                            String[] files = commentDao.files.split(',')
                            for (String filename : files) {
                                File file = new File(FileConstants.COMMENT_FILE_DIRECTORY, filename)
                                if (file.exists()) {
                                    commentDao.addToCommentFile(new CommentFileDAO(name: filename, content: file.bytes))
                                }
                            }
                            commentDao.save()
                        }
                    }
                }
            }
        }
    }
}
