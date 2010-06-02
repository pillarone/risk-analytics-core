package org.pillarone.riskanalytics.core.simulation.item.parameter

import org.pillarone.riskanalytics.core.parameter.Parameter
import org.pillarone.riskanalytics.core.parameterization.AbstractMultiDimensionalParameter
import org.pillarone.riskanalytics.core.parameter.MultiDimensionalParameter

class MultiDimensionalParameterHolder extends ParameterHolder {

    private AbstractMultiDimensionalParameter value;

    public MultiDimensionalParameterHolder(Parameter parameter) {
        super(parameter.path, parameter.periodIndex);
        this.value = parameter.parameterInstance
    }

    public MultiDimensionalParameterHolder(String path, int periodIndex, AbstractMultiDimensionalParameter value) {
        super(path, periodIndex);
        this.value = value;
    }

    AbstractMultiDimensionalParameter getBusinessObject() {
        return value;
    }

    void applyToDomainObject(Parameter parameter) {
        parameter.parameterInstance = value
    }

    Parameter createEmptyParameter() {
        return new MultiDimensionalParameter(path: path, periodIndex: periodIndex)
    }

    protected void updateValue(Object newValue) {
        value = newValue
    }

    public MultiDimensionalParameterHolder clone() {
        MultiDimensionalParameterHolder holder = (MultiDimensionalParameterHolder) super.clone();
        holder.value = (AbstractMultiDimensionalParameter) value.clone()
        return holder
    }

}
