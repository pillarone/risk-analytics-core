package org.pillarone.riskanalytics.core.parameter

class ParameterObjectParameter extends Parameter {

    EnumParameter type
    Object parameterObject

    static hasMany = [parameterEntries: ParameterEntry]

    static transients = ['parameterObject']

    public void setPeriodIndex(int index) {
        super.setPeriodIndex(index)
        type?.periodIndex = index
        parameterEntries.each {
            it.parameterEntryValue.periodIndex = index
        }

    }

    Class persistedClass() {
        ParameterObjectParameter
    }

}
