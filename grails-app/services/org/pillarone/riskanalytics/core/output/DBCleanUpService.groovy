package org.pillarone.riskanalytics.core.output

import org.pillarone.riskanalytics.core.BatchRun
import org.pillarone.riskanalytics.core.BatchRunSimulationRun
import org.pillarone.riskanalytics.core.parameter.comment.CommentTag
import org.pillarone.riskanalytics.core.parameter.comment.ParameterizationCommentDAO
import org.pillarone.riskanalytics.core.parameter.comment.ResultCommentDAO

class DBCleanUpService {

    boolean transactional = true

    def cleanUp() {
        println "starting db cleanup service."

        println "# comments before cleanup count : CommentTag = ${CommentTag.count()} ParameterizationCommentDAO = ${ParameterizationCommentDAO.count()} ResultCommentDAO = ${ResultCommentDAO.count()}"
        CommentTag.executeUpdate("delete ${CommentTag.name}".toString())
        ParameterizationCommentDAO.executeUpdate("delete ${ParameterizationCommentDAO.name}".toString())
        ResultCommentDAO.executeUpdate("delete ${ResultCommentDAO.name}".toString())
        println "# comments after cleanup count : CommentTag = ${CommentTag.count()} ParameterizationCommentDAO = ${ParameterizationCommentDAO.count()} ResultCommentDAO = ${ResultCommentDAO.count()}"

        println "# BatchRunSimulationRun before cleanup: ${BatchRunSimulationRun.count()}"
        BatchRunSimulationRun.executeUpdate("delete ${BatchRunSimulationRun.name}".toString())
        println "# BatchRunSimulationRun after cleanup: ${BatchRunSimulationRun.count()}"

        println "# simulations before cleanup: ${SimulationRun.count()}"
        SingleValueResult.executeUpdate("delete ${SingleValueResult.name}".toString())
        PostSimulationCalculation.executeUpdate("delete ${PostSimulationCalculation.name}".toString())
        SimulationRun.executeUpdate("delete ${SimulationRun.name}".toString())
        println "# simulations after cleanup: ${SimulationRun.count()}"

        println "# BatchRun before cleanup: ${BatchRun.count()}"
        BatchRun.executeUpdate("delete ${BatchRun.name}".toString())
        println "# BatchRun after cleanup: ${BatchRun.count()}"

        println "db cleanup service finished."
    }
}
