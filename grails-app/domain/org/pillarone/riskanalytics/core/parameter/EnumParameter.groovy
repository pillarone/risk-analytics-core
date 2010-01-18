package org.pillarone.riskanalytics.core.parameter

class EnumParameter extends Parameter {

    String parameterValue
    String parameterType

    Object parameterObject

    static transients = ['parameterObject']

    // todo: implements validation of type and value

    public Object getParameterInstance() {
        if (parameterValue != null && parameterType != null) {
            if (parameterObject == null) {
                parameterObject = EnumParameter.getClassLoader().loadClass(parameterType).valueOf(parameterValue)
                }
            return parameterObject
        }
        return null
    }

    public void setParameterInstance(Object value) {
        parameterType = value.class.name
        parameterValue = value.toString()
        parameterObject = null
    }

    void setParameterValue(String value) {
        this.@parameterValue = value
        parameterObject = null
    }

    Class persistedClass() {
        EnumParameter
    }

}
