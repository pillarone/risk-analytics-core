package org.pillarone.riskanalytics.core.output

import org.pillarone.riskanalytics.core.output.SimulationRun
import org.hibernate.FetchMode
import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.persistence.DateTimeMillisUserType

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
        date type: DateTimeMillisUserType
    }

    String toString() {
        "${path.pathName}, ${field.fieldName}, $value ($date)"
    }
}
