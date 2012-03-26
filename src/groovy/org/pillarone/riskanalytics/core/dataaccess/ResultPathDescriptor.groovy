package org.pillarone.riskanalytics.core.dataaccess

import org.pillarone.riskanalytics.core.output.PathMapping
import org.pillarone.riskanalytics.core.output.FieldMapping
import org.pillarone.riskanalytics.core.output.CollectorMapping


class ResultPathDescriptor {

    PathMapping path
    FieldMapping field
    CollectorMapping collector
    int period

    ResultPathDescriptor(PathMapping path, FieldMapping field, CollectorMapping collector, int period) {
        this.collector = collector
        this.field = field
        this.path = path
        this.period = period
    }



    @Override
    String toString() {
        return "${path?.pathName}:${field?.fieldName} (${collector?.collectorName}), P${period}"
    }


}
