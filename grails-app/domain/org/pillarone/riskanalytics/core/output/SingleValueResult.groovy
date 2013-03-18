package org.pillarone.riskanalytics.core.output

import org.hibernate.FetchMode
import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.util.DatabaseUtils

class SingleValueResult {

    SimulationRun simulationRun
    int period
    int iteration
    PathMapping path
    CollectorMapping collector
    FieldMapping field
    int valueIndex
    Double value
    DateTime date

    static constraints = {
        period min: 0
        iteration min: 0
        path()
        value()
        collector nullable: true
        field nullable: true
        date nullable: true
    }

    static mapping = {
        id generator: 'identity'
        path lazy: false, fetchMode: FetchMode.JOIN
        if (!DatabaseUtils.isOracleDatabase()) {
            date type: org.pillarone.riskanalytics.core.persistence.DateTimeMillisUserType
        } else {
            date type: org.pillarone.riskanalytics.core.persistence.DateTimeMillisUserType, column: 'date_time'
        }
    }

    String toString() {
        "${path.pathName}, ${field.fieldName}, $value ($date)"
    }
}
