package org.pillarone.riskanalytics.core.parameter

public class IntegerParameter extends Parameter {

    Integer integerValue

    public Object getParameterInstance() {
        return integerValue
    }

    public void setParameterInstance(Object value) { // this method has to be implemented to enable usage like new IntegerParameter(path:"p",parameterInstance:1)
        integerValue = (Integer) value
    }

    public void setParameterInstance(Integer value) {
        integerValue = value
    }

    public void setParameterInstance(int value) {
        integerValue = value
    }

    Class persistedClass() {
        IntegerParameter
    }

}
