package org.pillarone.riskanalytics.core.parameter

class ParameterEntry {

    String parameterEntryKey
    Parameter parameterEntryValue

    static belongsTo = [parameterObjectParameter: ParameterObjectParameter]

}
