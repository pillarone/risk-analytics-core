package org.pillarone.riskanalytics.core.simulation.item.parameter

import org.pillarone.riskanalytics.core.parameter.Parameter
import org.pillarone.riskanalytics.core.parameter.BooleanParameter

class BooleanParameterHolder extends ParameterHolder {

    private boolean value;

    public BooleanParameterHolder(Parameter parameter) {
        super(parameter.path, parameter.periodIndex);
        this.value = parameter.booleanValue
    }

    public BooleanParameterHolder(String path, int periodIndex, boolean value) {
        super(path, periodIndex);
        this.value = value;
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