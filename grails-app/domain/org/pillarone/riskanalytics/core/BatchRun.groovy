package org.pillarone.riskanalytics.core
import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.persistence.DateTimeMillisUserType
import org.pillarone.riskanalytics.core.util.DatabaseUtils

class BatchRun {
    String name
    String comment
    DateTime executionTime
    boolean executed = false

    static constraints = {
        name unique: true
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
