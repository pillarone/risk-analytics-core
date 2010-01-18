package org.pillarone.riskanalytics.core.parameter

import org.pillarone.riskanalytics.core.parameter.Parameter
import org.pillarone.riskanalytics.core.parameterization.ConstrainedString

class ConstrainedStringParameter extends Parameter {

    String markerClass
    String parameterValue

    public Object getParameterInstance() {
        if (markerClass != null && parameterValue != null) {
            return new ConstrainedString(getClass().getClassLoader().loadClass(markerClass), parameterValue)
        } else {
            return null
        }
    }

    public void setParameterInstance(ConstrainedString value) {
        parameterValue = value.stringValue
        markerClass = value.markerClass.name
    }

    public void setParameterInstance(Object value) {
        setParameterInstance(value as ConstrainedString)
    }

    Class persistedClass() {
        ConstrainedStringParameter
    }

}