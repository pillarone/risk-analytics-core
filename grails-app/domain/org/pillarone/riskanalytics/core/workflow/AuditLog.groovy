package org.pillarone.riskanalytics.core.workflow

import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.user.Person
import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.persistence.DateTimeMillisUserType
import org.pillarone.riskanalytics.core.util.DatabaseUtils

class AuditLog {

    ParameterizationDAO fromParameterization
    ParameterizationDAO toParameterization

    Status fromStatus
    Status toStatus

    DateTime date
    Person person

    static constraints = {
        fromParameterization(nullable: true)
        person(nullable: true)
    }

    String toString() {
        "${toParameterization.name}: ${fromStatus.displayName} -> ${toStatus.displayName}"
    }

    static mapping = {
        if(DatabaseUtils.isOracleDatabase()) {
            date type: DateTimeMillisUserType, column: 'date_time'
        } else {
            date type: DateTimeMillisUserType
        }
    }
}
