package org.pillarone.riskanalytics.core

import org.pillarone.riskanalytics.core.batch.BatchRunService
import org.pillarone.riskanalytics.core.persistence.DateTimeMillisUserType
import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.util.DatabaseUtils

class BatchRun {
    String name
    String comment
    DateTime executionTime
    boolean executed = false
    BatchRunService batchRunService

    static transients = ['batchRunService']

    static constraints = {
        name(unique: true)
        comment nullable: true
        executionTime nullable: true
    }

    static mapping = {
        executionTime type: DateTimeMillisUserType
        if (DatabaseUtils.isOracleDatabase()) {
            comment(column: 'comment_value')
        }
    }
}
