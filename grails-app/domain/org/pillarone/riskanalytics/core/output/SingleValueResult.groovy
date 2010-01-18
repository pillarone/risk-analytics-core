package org.pillarone.riskanalytics.core.output

import org.pillarone.riskanalytics.core.output.SimulationRun
import org.hibernate.FetchMode

class SingleValueResult {

    SimulationRun simulationRun
    int period
    int iteration
    PathMapping path
    CollectorMapping collector
    FieldMapping field
    int valueIndex
    Double value

    static constraints = {
        period min: 0
        iteration min: 0
        path()
        value()
        collector nullable:true
        field nullable:true
    }

    static mapping = {
        id generator: 'identity'
        path lazy: false, fetchMode: FetchMode.JOIN
    }
}
