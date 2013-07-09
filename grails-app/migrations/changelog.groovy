import org.pillarone.riskanalytics.core.FileConstants
import org.pillarone.riskanalytics.core.parameter.comment.CommentDAO
import org.pillarone.riskanalytics.core.parameter.comment.CommentFileDAO

databaseChangeLog = {
    changeSet(author: "detlef.brendle", id: "migrate-comment-files-to-db") {
        preConditions(onFail: 'MARK_RAN') {
            grailsPrecondition {
                check {
                    if (!(CommentDAO.list().any { it.files })) {
                        fail('No comment files found in DB.')
                    }
                }
            }
        }
        grailsChange {
            change {
                println 'Starting copy comment files into database.'
                CommentDAO.withNewSession {
                    CommentDAO.list().each { CommentDAO commentDao ->
                        if (commentDao.files) {
                            String[] files = commentDao.files.split(',')
                            for (String filename : files) {
                                File file = new File(FileConstants.COMMENT_FILE_DIRECTORY, filename)
                                if (file.exists()) {
                                    if (!(commentDao.commentFile.find { it.name == filename })) {
                                        log.info("Adding file $file.absolutePath to commentDAO")
                                        commentDao.addToCommentFile(new CommentFileDAO(name: filename, content: file.bytes))
                                    }
                                } else {
                                    log.error("File $file.absolutePath does not exist. Ignore")
                                }
                            }
                            commentDao.files = null
                            commentDao.save()
                        }
                    }
                }
            }
        }
    }
}
