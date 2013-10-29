package org.pillarone.riskanalytics.core.parameter.comment

import org.joda.time.DateTime
import org.junit.Test
import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.example.model.EmptyModel
import org.pillarone.riskanalytics.core.workflow.Status

import static org.junit.Assert.*

class CommentDAOTests {

    @Test
    void testSaveDelete() {

        int tagCount = Tag.count()
        int commentCount = CommentDAO.count()
        int commentTagCount = CommentTag.count()

        ParameterizationDAO parameterization = new ParameterizationDAO()
        parameterization.name = "test"
        parameterization.modelClassName = EmptyModel.name
        parameterization.itemVersion = "1"
        parameterization.periodCount = 1
        parameterization.status = Status.NONE
        assertNotNull parameterization.save()

        Tag tag = new Tag(name: 'tag').save()
        assertNotNull tag

        ParameterizationCommentDAO comment = new ParameterizationCommentDAO()
        comment.parameterization = parameterization
        comment.path = "path"
        comment.periodIndex = 0
        comment.timeStamp = new DateTime()
        comment.comment = "text"

        comment.addToTags(new CommentTag(tag: tag))

        assertNotNull comment.save()

        assertEquals tagCount + 1, Tag.count()
        assertEquals commentCount + 1, ParameterizationCommentDAO.count()
        assertEquals commentTagCount + 1, CommentTag.count()

        comment.delete()

        assertEquals tagCount + 1, Tag.count()
        assertEquals commentCount, ParameterizationCommentDAO.count()
        assertEquals commentTagCount, CommentTag.count()

    }

    @Test
    void testFileHandling() {
        String fileContent = 'test content'
        ParameterizationDAO parameterization = new ParameterizationDAO()
        parameterization.name = "test"
        parameterization.modelClassName = EmptyModel.name
        parameterization.itemVersion = "1"
        parameterization.periodCount = 1
        parameterization.status = Status.NONE
        assertNotNull parameterization.save()
        File file = new File('test.txt')
        file.text = fileContent

        ParameterizationCommentDAO comment = new ParameterizationCommentDAO()
        comment.parameterization = parameterization
        comment.path = "path"
        comment.periodIndex = 0
        comment.timeStamp = new DateTime()
        comment.comment = "text"
        comment.addToCommentFile(new CommentFileDAO(name:file.name, content: file.bytes))

        assertNotNull comment.save(flush: true)
        int commentId = comment.id
        CommentDAO.withNewSession {
            CommentDAO persistedComment = CommentDAO.get(commentId)
            assertEquals(1, persistedComment.commentFile.size())
            CommentFileDAO commentFile = (persistedComment.commentFile as List)[0]
            assertEquals('test.txt', commentFile.name)
            assertEquals(fileContent, new String(commentFile.content))
        }
    }

    @Test
    void testDeleteComment() {
        String fileContent = 'test content'
        ParameterizationDAO parameterization = new ParameterizationDAO()
        parameterization.name = "test"
        parameterization.modelClassName = EmptyModel.name
        parameterization.itemVersion = "1"
        parameterization.periodCount = 1
        parameterization.status = Status.NONE
        assertNotNull parameterization.save()
        File file = new File('test.txt')
        file.text = fileContent

        ParameterizationCommentDAO comment = new ParameterizationCommentDAO()
        comment.parameterization = parameterization
        comment.path = "path"
        comment.periodIndex = 0
        comment.timeStamp = new DateTime()
        comment.comment = "text"
        comment.addToCommentFile(new CommentFileDAO(name:file.name, content: file.bytes))

        assertNotNull comment.save(flush: true)
        assert 1 == CommentFileDAO.list().size()

        comment.delete(flush: true)
        CommentDAO.withNewSession {
            assert 0 == CommentFileDAO.list().size()
        }
    }
}
