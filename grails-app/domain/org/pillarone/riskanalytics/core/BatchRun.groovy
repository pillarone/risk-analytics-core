package org.pillarone.riskanalytics.core

import org.pillarone.riskanalytics.core.output.SimulationRun
import org.pillarone.riskanalytics.core.util.DatabaseUtils

class BatchRun {
    String name
    String comment
    boolean executed = false
    List<SimulationRun> simulationRuns = []

    static hasMany = [simulationRuns: SimulationRun]

    static fetchMode = [simulationRuns: 'eager']

    static constraints = {
        name unique: true
        comment nullable: true
    }

    static mapping = {
        simulationRuns indexColumn: [
                name: "priority",
                type: Integer
        ]
        if (DatabaseUtils.oracleDatabase) {
            comment(column: 'comment_value')
        }
    }
}
