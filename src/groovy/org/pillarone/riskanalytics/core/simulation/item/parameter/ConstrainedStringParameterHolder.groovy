package org.pillarone.riskanalytics.core.simulation.item.parameter

import org.pillarone.riskanalytics.core.parameter.Parameter
import org.pillarone.riskanalytics.core.parameterization.ConstrainedString
import org.pillarone.riskanalytics.core.parameter.ConstrainedStringParameter
import org.pillarone.riskanalytics.core.components.ComponentUtils

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

    @Override
    void clearCachedValues() {
        value.selectedComponent = null
    }

    List<String> referencePaths(Class markerInterface, String refValue) {
        List<String> paths = new ArrayList()
        if (markerInterface.is(value.markerClass) && ComponentUtils.getNormalizedName(value.stringValue).equals(refValue)) {
            paths.add(path)
        }
        return paths
    }

    List<String> updateReferenceValues(Class markerInterface, String oldValue, String newValue) {
        List<String> paths = referencePaths(markerInterface, oldValue)
        if (paths) {
            setValue(ComponentUtils.getModelName(newValue, ComponentUtils.SUB))
        }
        return paths
    }
}
