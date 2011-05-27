package org.pillarone.riskanalytics.core.simulation.item.parameter

import org.pillarone.riskanalytics.core.parameter.Parameter
import org.pillarone.riskanalytics.core.parameter.EnumParameter

class EnumParameterHolder extends ParameterHolder {

    private Enum value

    public EnumParameterHolder(Parameter parameter) {
        super(parameter);
    }

    public EnumParameterHolder(String path, int periodIndex, Enum value) {
        super(path, periodIndex);
        this.value = value;
    }

    @Override
    void setParameter(Parameter parameter) {
        this.value = Enum.valueOf(Thread.currentThread().contextClassLoader.loadClass(parameter.parameterType), parameter.parameterValue)
    }

    Enum getBusinessObject() {
        return value;
    }

    void applyToDomainObject(Parameter parameter) {
        parameter.parameterType = value.getClass().getName()
        parameter.parameterValue = value.toString()
    }

    Parameter createEmptyParameter() {
        return new EnumParameter(path: path, periodIndex: periodIndex)
    }

    protected void updateValue(Object newValue) {
        value = Enum.valueOf(value.getClass(), newValue)
    }

}
