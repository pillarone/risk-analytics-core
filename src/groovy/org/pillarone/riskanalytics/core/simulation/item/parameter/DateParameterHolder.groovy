package org.pillarone.riskanalytics.core.simulation.item.parameter

import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.parameter.Parameter
import org.pillarone.riskanalytics.core.parameter.DateParameter

class DateParameterHolder extends ParameterHolder {

    private DateTime value;

    public DateParameterHolder(Parameter parameter) {
        super(parameter);
    }

    public DateParameterHolder(String path, int periodIndex, DateTime value) {
        super(path, periodIndex);
        this.value = value;
    }

    @Override
    void setParameter(Parameter parameter) {
        this.value = parameter.dateValue
    }

    DateTime getBusinessObject() {
        return value;
    }

    void applyToDomainObject(Parameter parameter) {
        parameter.dateValue = value
    }

    Parameter createEmptyParameter() {
        return new DateParameter(path: path, periodIndex: periodIndex)
    }

    protected void updateValue(Object newValue) {
        value = newValue
    }

    public DateParameterHolder clone() {
        DateParameterHolder holder = (DateParameterHolder) super.clone();
        holder.value = new DateTime(value.getMillis())
        return holder
    }

}
