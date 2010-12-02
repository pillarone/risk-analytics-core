package org.pillarone.riskanalytics.core.simulation.engine.grid.output;

import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;

public class ResultDescriptor implements Serializable {

    public static final char SEPARATOR = '_';

    private long pathId, fieldId, period;
    private String path;

    public ResultDescriptor(long fieldId, long pathId, long period) {
        this.fieldId = fieldId;
        this.path = pathId+"";
        this.pathId = pathId;
        this.period = period;
    }

    public ResultDescriptor(long fieldId, String path, long period) {
        this.fieldId = fieldId;
        this.path = path;
        this.period = period;
    }

    public long getFieldId() {
        return fieldId;
    }

    public void setFieldId(long fieldId) {
        this.fieldId = fieldId;
    }

    public String getPath(){
        return path;
    }

    public long getPathId() {
        return pathId;
    }

    public void setPathId(long pathId) {
        this.pathId = pathId;
    }

    public long getPeriod() {
        return period;
    }

    public void setPeriod(long period) {
        this.period = period;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ResultDescriptor) {
            ResultDescriptor resultDescriptor = (ResultDescriptor) obj;
            return resultDescriptor.getFieldId() == fieldId &&
                    resultDescriptor.getPath().equals(path) &&
                    resultDescriptor.getPeriod() == period;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(fieldId).append(path).append(period).toHashCode();
    }

    public String getFileName() {
        return new StringBuilder().append(pathId).append(SEPARATOR).append(period).append(SEPARATOR).append(fieldId).toString();
    }

    @Override
    public String toString() {
        return "Period: " + period + ", path: " + pathId + ", field: " + fieldId;
    }
}
