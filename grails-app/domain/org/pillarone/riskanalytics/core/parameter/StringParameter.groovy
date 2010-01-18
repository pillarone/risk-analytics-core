package org.pillarone.riskanalytics.core.parameter

import org.pillarone.riskanalytics.core.parameter.Parameter

public class StringParameter extends Parameter {

    String parameterValue

    public Object getParameterInstance() {
        return parameterValue
    }

    public void setParameterInstance(String value) {
        parameterValue = value
    }

    public void setParameterInstance(Object value) {
        parameterValue = value.toString()
    }

    Class persistedClass() {
        StringParameter
    }

}