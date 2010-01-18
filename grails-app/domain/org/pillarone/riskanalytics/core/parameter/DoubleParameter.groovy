package org.pillarone.riskanalytics.core.parameter

class DoubleParameter extends Parameter {

    Double doubleValue

    public Object getParameterInstance() {
        return doubleValue
    }

    public void setParameterInstance(Double value) {
        doubleValue = value
    }

    public void setParameterInstance(Object value) {
        setParameterInstance value as Number
    }

    public void setParameterInstance(Number value) {
        doubleValue = value.doubleValue()
    }

    public void setParameterInstance(double value) {
        doubleValue = new Double(value)
    }

    Class persistedClass() {
        DoubleParameter
    }

}
