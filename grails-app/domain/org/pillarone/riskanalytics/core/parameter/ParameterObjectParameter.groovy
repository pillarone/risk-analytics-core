package org.pillarone.riskanalytics.core.parameter

import org.pillarone.riskanalytics.core.parameterization.IParameterObjectClassifier
import org.pillarone.riskanalytics.core.parameterization.IParameterObject
import org.pillarone.riskanalytics.core.parameterization.ParameterizationHelper

class ParameterObjectParameter extends Parameter {

    EnumParameter type
    Object parameterObject

    static hasMany = [parameterEntries: ParameterEntry]

    static transients = ['parameterObject']


    public Map parameterMap() {
        def result = [:]
        parameterEntries.each {entry ->
            result[entry.parameterEntryKey] = entry.parameterEntryValue.getParameterInstance()
        }
        return result
    }

    private Map parameterEntriesMap() {
        def result = [:]
        parameterEntries.each {entry ->
            result[entry.parameterEntryKey] = entry
        }
        return result
    }

    public Object getParameterInstance() {
        if (parameterObject == null) {
            parameterObject = type.getParameterInstance().getParameterObject(parameterMap())
        }
        return parameterObject
    }

    public void setParameterInstance(Object value) {
        setParameterInstance(value as IParameterObject)
        parameterObject = null
    }

    public void setParameterInstance(IParameterObject value) {
        if (type == null) {
            type = new EnumParameter(path: "$path:type")
        }
        type.parameterInstance = value.type
        Map parameterEntries = parameterEntriesMap()
        value.parameters.each {parameterKey, parameterValue ->
            ParameterEntry entry = parameterEntries[parameterKey]
            if (entry == null) {
                entry = new ParameterEntry(parameterEntryKey: parameterKey)
                entry.parameterEntryValue = ParameterizationHelper.getParameter("$path:$parameterKey", parameterValue)
                addToParameterEntries(entry)
            } else {
                entry.parameterEntryValue.parameterInstance = parameterValue
            }
        }
        parameterObject = null
    }

    public void setPeriodIndex(int index) {
        super.setPeriodIndex(index)
        type?.periodIndex = index
        parameterEntries.each {
            it.parameterEntryValue.periodIndex = index
        }

    }

    public void setParameterInstance(IParameterObjectClassifier classifier, Map parameters) {
        if (type == null) {
            type = new EnumParameter(path: "$path:type", periodIndex: periodIndex)
        }
        type.parameterInstance = classifier
        Map parameterEntries = parameterEntriesMap()
        parameters.each {parameterKey, parameterValue ->
            ParameterEntry entry = parameterEntries[parameterKey]
            if (entry == null) {
                entry = new ParameterEntry(parameterEntryKey: parameterKey)
                entry.parameterEntryValue = ParameterizationHelper.getParameter("$path:$parameterKey", parameterValue)
                addToParameterEntries(entry)
            } else {
                entry.parameterEntryValue.parameterInstance = parameterValue
            }
            entry.parameterEntryValue.periodIndex = periodIndex
        }
        parameterObject = null
    }

    Class persistedClass() {
        ParameterObjectParameter
    }

}
