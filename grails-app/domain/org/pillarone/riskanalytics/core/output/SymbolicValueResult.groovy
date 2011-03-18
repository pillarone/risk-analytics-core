package org.pillarone.riskanalytics.core.output

import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.persistence.DateTimeMillisUserType

class SymbolicValueResult {
    String path
    String collector
    String field
    SimulationRun simulationRun
    int period
    Double value
    DateTime date
    int iteration

    static mapping = {
        date type: DateTimeMillisUserType
    }
}