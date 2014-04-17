package org.pillarone.riskanalytics.core

import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.output.SimulationRun
import org.pillarone.riskanalytics.core.persistence.DateTimeMillisUserType
import org.pillarone.riskanalytics.core.user.Person
import org.pillarone.riskanalytics.core.util.DatabaseUtils

class BatchRun {
    String name
    String comment
    boolean executed = false
    List<SimulationRun> simulationRuns = []

    DateTime creationDate
    DateTime modificationDate
    Person creator
    Person lastUpdater

    static hasMany = [simulationRuns: SimulationRun]

    static fetchMode = [simulationRuns: 'eager']

    static constraints = {
        name unique: true
        comment nullable: true
        creationDate nullable: true
        modificationDate nullable: true
        creator nullable: true
        lastUpdater nullable: true
    }

    static mapping = {
        simulationRuns indexColumn: [
                name: "priority",
                type: Integer
        ]
        creationDate type: DateTimeMillisUserType
        creator lazy: false
        modificationDate type: DateTimeMillisUserType
        if (DatabaseUtils.oracleDatabase) {
            comment(column: 'comment_value')
        }
    }
}
