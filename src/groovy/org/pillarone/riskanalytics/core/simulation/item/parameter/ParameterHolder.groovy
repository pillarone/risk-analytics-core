package org.pillarone.riskanalytics.core.simulation.item.parameter

import org.pillarone.riskanalytics.core.parameter.Parameter

abstract class ParameterHolder implements Cloneable{

    String path
    int periodIndex

    boolean modified
    boolean added
    boolean removed

    public ParameterHolder(Parameter parameter) {
        this(parameter.path, parameter.periodIndex)
        setParameter(parameter)
    }

    public ParameterHolder(String path, int periodIndex) {
        this.path = path
        this.periodIndex = periodIndex
        this.modified = false
        this.added = false
        this.removed = false
    }

    abstract void setParameter(Parameter parameter)

    abstract Object getBusinessObject()

    final void setValue(Object newValue) {
        if (!added) {
            modified = true
        }
        if (removed) {
            throw new IllegalStateException("Attempting to update value on deleted parameter holder")
        }
        updateValue(newValue)
    }

    //todo really needed??
    final void setModified(boolean value) {
        if(!added) {
            this.@modified = value
        }
    }

    abstract protected void updateValue(Object newValue)

    abstract void applyToDomainObject(Parameter parameter)

    abstract Parameter createEmptyParameter()

    boolean hasParameterChanged() {
        return modified && !added
    }

    public Object clone() {
        ParameterHolder holder = (ParameterHolder) super.clone()
        holder.modified = false
        holder.added = false
        holder.removed = false
        return holder;
    }

    public void clearCachedValues() { }

    String toString() {
        return path
    }


}
