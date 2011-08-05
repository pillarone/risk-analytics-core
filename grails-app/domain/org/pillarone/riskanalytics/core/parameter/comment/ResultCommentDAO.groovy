package org.pillarone.riskanalytics.core.parameter.comment

import org.pillarone.riskanalytics.core.output.SimulationRun

class ResultCommentDAO extends CommentDAO {

    SimulationRun simulationRun

    String function

    static belongsTo = SimulationRun

    static constraints = {
        function(nullable: true)
    }

}
