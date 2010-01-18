package org.pillarone.riskanalytics.core.simulation.item.parameter

import org.pillarone.riskanalytics.core.parameter.Parameter
import org.pillarone.riskanalytics.core.parameter.IntegerParameter

class IntegerParameterHolder extends ParameterHolder {

    private int value;

    public IntegerParameterHolder(Parameter parameter) {
        super(parameter.path, parameter.periodIndex);
        this.value = parameter.integerValue
    }

    public IntegerParameterHolder(String path, int periodIndex, int value) {
        super(path, periodIndex);
        this.value = value;
    }

    Integer getBusinessObject() {
        return value;
    }

    void applyToDomainObject(Parameter parameter) {
        parameter.integerValue = value
    }

    Parameter createEmptyParameter() {
        return new IntegerParameter(path: path, periodIndex: periodIndex)
    }

    protected void updateValue(Object newValue) {
        value = newValue
    }


}
