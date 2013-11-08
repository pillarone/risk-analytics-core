package org.pillarone.riskanalytics.core.simulation.item.parameter

import groovy.transform.CompileStatic
import org.pillarone.riskanalytics.core.parameter.ConstrainedStringParameter
import org.pillarone.riskanalytics.core.parameter.Parameter
import org.pillarone.riskanalytics.core.parameterization.ConstrainedString

class ConstrainedStringParameterHolder extends ParameterHolder implements IMarkerValueAccessor {

    private ConstrainedString value;

    public ConstrainedStringParameterHolder(Parameter parameter) {
        super(parameter);
    }

    public ConstrainedStringParameterHolder(String path, int periodIndex, ConstrainedString value) {
        super(path, periodIndex);
        this.value = value;
    }

    @Override
    void setParameter(Parameter parameter) {
        this.value = new ConstrainedString(getClass().getClassLoader().loadClass(parameter.markerClass), parameter.parameterValue)
    }

    @CompileStatic
    ConstrainedString getBusinessObject() {
        return value;
    }

    void applyToDomainObject(Parameter parameter) {
        parameter.markerClass = value.markerClass.name
        parameter.parameterValue = value.stringValue
    }

    @CompileStatic
    Parameter createEmptyParameter() {
        return new ConstrainedStringParameter(path: path, periodIndex: periodIndex)
    }

    @CompileStatic
    protected void updateValue(Object newValue) {
        value.stringValue = newValue.toString()
    }

    @CompileStatic
    public ConstrainedStringParameterHolder clone() {
        ConstrainedStringParameterHolder holder = (ConstrainedStringParameterHolder) super.clone();
        //don't call setValue here!
        holder.@value = new ConstrainedString(value.markerClass, value.stringValue)
        return holder
    }

    @Override
    @CompileStatic
    void clearCachedValues() {
        value.selectedComponent = null
    }

    @CompileStatic
    List<String> referencePaths(Class markerInterface, String refValue) {
        List<String> paths = new ArrayList()
        if (markerInterface.is(value.markerClass) && value.stringValue == refValue) {
            paths.add(path)
        }
        return paths
    }

    @CompileStatic
    List<String> updateReferenceValues(Class markerInterface, String oldValue, String newValue) {
        List<String> paths = referencePaths(markerInterface, oldValue)
        if (paths) {
            setValue(newValue)
        }
        return paths
    }
}
