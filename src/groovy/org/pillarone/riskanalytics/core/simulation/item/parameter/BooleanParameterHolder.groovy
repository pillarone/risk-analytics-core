package org.pillarone.riskanalytics.core.simulation.item.parameter

import org.pillarone.riskanalytics.core.parameter.Parameter
import org.pillarone.riskanalytics.core.parameter.BooleanParameter

class BooleanParameterHolder extends ParameterHolder {

    private boolean value;

    public BooleanParameterHolder(Parameter parameter) {
        super(parameter);
    }

    public BooleanParameterHolder(String path, int periodIndex, boolean value) {
        super(path, periodIndex);
        this.value = value;
    }

    @Override
    void setParameter(Parameter parameter) {
        this.value = parameter.booleanValue
    }

    Boolean getBusinessObject() {
        return value;
    }

    void applyToDomainObject(Parameter parameter) {
        parameter.booleanValue = value
    }

    Parameter createEmptyParameter() {
        return new BooleanParameter(path: path, periodIndex: periodIndex)
    }

    protected void updateValue(Object newValue) {
        value = newValue
    }

}