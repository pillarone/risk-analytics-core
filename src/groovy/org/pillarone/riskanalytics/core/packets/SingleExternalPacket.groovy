package org.pillarone.riskanalytics.core.packets

import groovy.transform.CompileStatic
import org.pillarone.riskanalytics.core.dataaccess.DateTimeValuePair

@CompileStatic
class SingleExternalPacket extends ExternalPacket {

    private Map<String, List<DateTimeValuePair>> values = [:]

    void addValue(String field, List<DateTimeValuePair> values) {
        this.values.put(field, values)
    }

    List<DateTimeValuePair> getValues(String field) {
        return values[field]
    }
}
