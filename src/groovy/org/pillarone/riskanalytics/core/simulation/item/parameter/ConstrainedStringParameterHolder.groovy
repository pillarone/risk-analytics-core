package org.pillarone.riskanalytics.core.simulation.item.parameter

import org.pillarone.riskanalytics.core.parameter.Parameter
import org.pillarone.riskanalytics.core.parameterization.ConstrainedString
import org.pillarone.riskanalytics.core.parameter.ConstrainedStringParameter

class ConstrainedStringParameterHolder extends ParameterHolder {

    private ConstrainedString value;

    public ConstrainedStringParameterHolder(Parameter parameter) {
        super(parameter.path, parameter.periodIndex);
        this.value = new ConstrainedString(getClass().getClassLoader().loadClass(parameter.markerClass), parameter.parameterValue)
    }

    public ConstrainedStringParameterHolder(String path, int periodIndex, ConstrainedString value) {
        super(path, periodIndex);
        this.value = value;
    }

    ConstrainedString getBusinessObject() {
        return value;
    }

    void applyToDomainObject(Parameter parameter) {
        parameter.markerClass = value.markerClass.name
        parameter.parameterValue = value.stringValue
    }

    Parameter createEmptyParameter() {
        return new ConstrainedStringParameter(path: path, periodIndex: periodIndex)
    }

    protected void updateValue(Object newValue) {
        value.stringValue = newValue.toString()
    }

    public ConstrainedStringParameterHolder clone() {
        ConstrainedStringParameterHolder holder = (ConstrainedStringParameterHolder) super.clone();
        //don't call setValue here!
        holder.@value = new ConstrainedString(value.markerClass, value.stringValue)
        return holder
    }

}
