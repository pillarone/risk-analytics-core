package org.pillarone.riskanalytics.core.simulation.item.parameter

import groovy.transform.CompileStatic
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

    @CompileStatic
    Double getBusinessObject() {
        return value;
    }

    void applyToDomainObject(Parameter parameter) {
        parameter.doubleValue = value
    }

    @CompileStatic
    Parameter createEmptyParameter() {
        return new DoubleParameter(path: path, periodIndex: periodIndex)
    }

    @CompileStatic
    protected void updateValue(Object newValue) {
        value = (double) newValue
    }

}
