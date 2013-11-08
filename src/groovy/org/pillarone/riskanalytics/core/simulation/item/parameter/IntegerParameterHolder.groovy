package org.pillarone.riskanalytics.core.simulation.item.parameter

import groovy.transform.CompileStatic
import org.pillarone.riskanalytics.core.parameter.Parameter
import org.pillarone.riskanalytics.core.parameter.IntegerParameter

class IntegerParameterHolder extends ParameterHolder {

    private int value;

    public IntegerParameterHolder(Parameter parameter) {
        super(parameter);
    }

    public IntegerParameterHolder(String path, int periodIndex, int value) {
        super(path, periodIndex);
        this.value = value;
    }

    @Override
    void setParameter(Parameter parameter) {
        this.value = parameter.integerValue
    }

    @CompileStatic
    Integer getBusinessObject() {
        return value;
    }

    void applyToDomainObject(Parameter parameter) {
        parameter.integerValue = value
    }

    @CompileStatic
    Parameter createEmptyParameter() {
        return new IntegerParameter(path: path, periodIndex: periodIndex)
    }

    @CompileStatic
    protected void updateValue(Object newValue) {
        value = (int) newValue
    }


}
