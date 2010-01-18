package org.pillarone.riskanalytics.core.parameter

import org.pillarone.riskanalytics.core.ParameterizationDAO

class Parameter {

    String path
    Integer periodIndex = 0

    static belongsTo = [ParameterizationDAO, ParameterEntry, ParameterObjectParameter, MultiDimensionalParameterValue]

    static transients = ['parameterInstance']

    public Object getParameterInstance() {
        throw new UnsupportedOperationException("${this.class.name}: getParameterInstance not implemented")
    }

    public void setParameterInstance(Object value) {
        throw new UnsupportedOperationException("${this.class.name}: setParameterInstance not implemented for ${value.class.name}: $value")
    }

    Class persistedClass() {
        Parameter
    }


}
