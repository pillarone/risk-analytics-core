package org.pillarone.riskanalytics.core.parameter.comment

import org.pillarone.riskanalytics.core.output.SimulationRun
import org.pillarone.riskanalytics.core.util.DatabaseUtils

class ResultCommentDAO extends CommentDAO {

    SimulationRun simulationRun

    String function

    static belongsTo = SimulationRun

    static constraints = {
        function(nullable: true)
    }

    static mapping = {
        if (DatabaseUtils.isMsSqlDatabase()) {
            function(column: 'funct')
        }
    }

}
