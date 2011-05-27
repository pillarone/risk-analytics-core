package org.pillarone.riskanalytics.core.simulation.item.parameter

import org.pillarone.riskanalytics.core.parameter.Parameter
import org.pillarone.riskanalytics.core.parameter.DoubleParameter

class DoubleParameterHolder extends ParameterHolder {

    private double value;

    public DoubleParameterHolder(Parameter parameter) {
        super(parameter);
    }

    public DoubleParameterHolder(String path, int periodIndex, double value) {
        super(path, periodIndex);
        this.value = value;
    }

    @Override
    void setParameter(Parameter parameter) {
        this.value = parameter.doubleValue
    }

    Double getBusinessObject() {
        return value;
    }

    void applyToDomainObject(Parameter parameter) {
        parameter.doubleValue = value
    }

    Parameter createEmptyParameter() {
        return new DoubleParameter(path: path, periodIndex: periodIndex)
    }

    protected void updateValue(Object newValue) {
        value = newValue
    }

}
