package org.pillarone.riskanalytics.core.workflow

import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.user.Person

class AuditLog {

    ParameterizationDAO fromParameterization
    ParameterizationDAO toParameterization

    Status fromStatus
    Status toStatus

    Date date
    Person person

    static constraints = {
        fromParameterization(nullable: true)
        person(nullable: true)
    }

    String toString() {
        "${toParameterization.name}: ${fromStatus.displayName} -> ${toStatus.displayName}"
    }
}
