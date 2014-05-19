package org.pillarone.riskanalytics.core.packets

import groovy.transform.CompileStatic

@CompileStatic
class AggregatedExternalPacket extends ExternalPacket {

    private Map<String, Double> values = [:]

    void addValue(String field, double value) {
        values.put(field, value)
    }

    Double getValue(String field) {
        return values[field]
    }

}
