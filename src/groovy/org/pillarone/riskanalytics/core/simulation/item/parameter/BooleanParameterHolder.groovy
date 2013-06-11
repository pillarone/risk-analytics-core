package org.pillarone.riskanalytics.core.simulation.item.parameter

import groovy.transform.CompileStatic
import org.pillarone.riskanalytics.core.parameter.Parameter
import org.pillarone.riskanalytics.core.parameter.BooleanParameter

class BooleanParameterHolder extends ParameterHolder {

    private boolean value;

    @CompileStatic
    public BooleanParameterHolder(Parameter parameter) {
        super(parameter);
    }

    @CompileStatic
    public BooleanParameterHolder(String path, int periodIndex, boolean value) {
        super(path, periodIndex);
        this.value = value;
    }

    @Override
    void setParameter(Parameter parameter) {
        this.value = parameter.booleanValue
    }

    @CompileStatic
    Boolean getBusinessObject() {
        return value;
    }

    void applyToDomainObject(Parameter parameter) {
        parameter.booleanValue = value
    }

    @CompileStatic
    Parameter createEmptyParameter() {
        return new BooleanParameter(path: path, periodIndex: periodIndex)
    }

    @CompileStatic
    protected void updateValue(Object newValue) {
        value = newValue
    }

}