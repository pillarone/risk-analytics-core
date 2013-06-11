package org.pillarone.riskanalytics.core.simulation.item.parameter

import groovy.transform.CompileStatic
import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.parameter.Parameter
import org.pillarone.riskanalytics.core.parameter.DateParameter

class DateParameterHolder extends ParameterHolder {

    private DateTime value;

    @CompileStatic
    public DateParameterHolder(Parameter parameter) {
        super(parameter);
    }

    @CompileStatic
    public DateParameterHolder(String path, int periodIndex, DateTime value) {
        super(path, periodIndex);
        this.value = value;
    }

    @Override
    void setParameter(Parameter parameter) {
        this.value = parameter.dateValue
    }

    @CompileStatic
    DateTime getBusinessObject() {
        return value;
    }

    void applyToDomainObject(Parameter parameter) {
        parameter.dateValue = value
    }

    @CompileStatic
    Parameter createEmptyParameter() {
        return new DateParameter(path: path, periodIndex: periodIndex)
    }

    @CompileStatic
    protected void updateValue(Object newValue) {
        value = (DateTime) newValue
    }

    @CompileStatic
    public DateParameterHolder clone() {
        DateParameterHolder holder = (DateParameterHolder) super.clone();
        holder.value = new DateTime(value.getMillis())
        return holder
    }

}
