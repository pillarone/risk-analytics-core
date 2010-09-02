package org.pillarone.riskanalytics.core.parameter.comment

import org.pillarone.riskanalytics.core.example.model.EmptyModel
import org.pillarone.riskanalytics.core.ParameterizationDAO


class CommentDAOTests extends GroovyTestCase {

    void testSaveDelete() {

        int tagCount = Tag.count()
        int commentCount = CommentDAO.count()
        int commentTagCount = CommentTag.count()

        ParameterizationDAO parameterization = new ParameterizationDAO()
        parameterization.name = "test"
        parameterization.modelClassName = EmptyModel.name
        parameterization.itemVersion = "1"
        parameterization.periodCount = 1
        assertNotNull parameterization.save()

        Tag tag = new Tag(name: 'tag').save()
        assertNotNull tag
        
        CommentDAO comment = new CommentDAO()
        comment.parameterization = parameterization
        comment.path = "path"
        comment.periodIndex = 0
        comment.timeStamp = new Date()
        comment.comment = "text"

        comment.addToTags(new CommentTag(tag: tag))

        assertNotNull comment.save()

        assertEquals tagCount + 1, Tag.count()
        assertEquals commentCount + 1, CommentDAO.count()
        assertEquals commentTagCount + 1, CommentTag.count()

        comment.delete()

        assertEquals tagCount + 1, Tag.count()
        assertEquals commentCount, CommentDAO.count()
        assertEquals commentTagCount, CommentTag.count()

    }
}
