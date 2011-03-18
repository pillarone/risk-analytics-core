package org.pillarone.riskanalytics.core.workflow

import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.user.Person
import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.persistence.DateTimeMillisUserType

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
        date type: DateTimeMillisUserType
    }
}
