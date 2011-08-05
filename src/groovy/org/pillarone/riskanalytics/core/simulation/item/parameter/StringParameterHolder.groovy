package org.pillarone.riskanalytics.core.simulation.item.parameter

import org.pillarone.riskanalytics.core.parameter.Parameter
import org.pillarone.riskanalytics.core.parameter.StringParameter

class StringParameterHolder extends ParameterHolder {

    private String value;

    public StringParameterHolder(Parameter parameter) {
        super(parameter);
    }

    public StringParameterHolder(String path, int periodIndex, String value) {
        super(path, periodIndex);
        this.value = value;
    }

    @Override
    void setParameter(Parameter parameter) {
        this.value = parameter.parameterValue
    }

    String getBusinessObject() {
        return value;
    }

    void applyToDomainObject(Parameter parameter) {
        parameter.parameterValue = value
    }

    Parameter createEmptyParameter() {
        return new StringParameter(path: path, periodIndex: periodIndex)
    }

    protected void updateValue(Object newValue) {
        value = newValue.toString()
    }

}
