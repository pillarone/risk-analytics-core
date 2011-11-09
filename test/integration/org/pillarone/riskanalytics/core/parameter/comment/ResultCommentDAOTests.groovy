package org.pillarone.riskanalytics.core.parameter.comment

import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.output.SimulationRun
import models.core.CoreModel

/**
 * @author fouad.jaada@intuitive-collaboration.com
 */
class ResultCommentDAOTests extends GroovyTestCase {


    void testSaveDeleteResultCommentDAO() {
        int tagCount = Tag.count()
        int commentCount = CommentDAO.count()
        int commentTagCount = CommentTag.count()

        SimulationRun simulationRun = new SimulationRun(name: "testRun", model: CoreModel.name, startTime: new DateTime())
        simulationRun.save()

        Tag tag = new Tag(name: 'tag').save()
        assertNotNull tag

        ResultCommentDAO comment = new ResultCommentDAO()
        comment.simulationRun = simulationRun
        comment.path = "path"
        comment.periodIndex = 0
        comment.timeStamp = new DateTime()
        comment.comment = "text"

        comment.addToTags(new CommentTag(tag: tag))

        assertNotNull comment.save()

        assertEquals tagCount + 1, Tag.count()
        assertEquals commentCount + 1, ResultCommentDAO.count()
        assertEquals commentTagCount + 1, CommentTag.count()

        comment.delete()

        assertEquals tagCount + 1, Tag.count()
        assertEquals commentCount, ResultCommentDAO.count()
        assertEquals commentTagCount, CommentTag.count()

    }

}
