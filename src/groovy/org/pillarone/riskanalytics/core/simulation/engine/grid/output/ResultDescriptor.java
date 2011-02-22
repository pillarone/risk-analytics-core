package org.pillarone.riskanalytics.core.simulation.engine.grid.output;

import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;

public class ResultDescriptor implements Serializable {

    public static final char SEPARATOR = '_';

    private long pathId, fieldId, period, collectorId;
    private String path;

    public ResultDescriptor(long fieldId, long pathId, long collectorId, long period) {
        this.fieldId = fieldId;
        this.collectorId = collectorId;
        this.path = pathId + "";
        this.pathId = pathId;
        this.period = period;
    }

    public ResultDescriptor(long fieldId, String path, long collectorId, long period) {
        this.fieldId = fieldId;
        this.collectorId = collectorId;
        this.path = path;
        this.period = period;
    }

    public long getFieldId() {
        return fieldId;
    }

    public void setFieldId(long fieldId) {
        this.fieldId = fieldId;
    }

    public String getPath() {
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

    public long getCollectorId() {
        return collectorId;
    }

    public void setCollectorId(long collectorId) {
        this.collectorId = collectorId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ResultDescriptor) {
            ResultDescriptor resultDescriptor = (ResultDescriptor) obj;
            return resultDescriptor.fieldId == fieldId &&
                    resultDescriptor.path.equals(path) &&
                    resultDescriptor.collectorId == collectorId &&
                    resultDescriptor.period == period;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(fieldId).append(path).append(period).append(collectorId).toHashCode();
    }

    public String getFileName() {
        return new StringBuilder().append(pathId).append(SEPARATOR).append(period).append(SEPARATOR).append(fieldId)
                .append(SEPARATOR).append(collectorId).toString();
    }

    @Override
    public String toString() {
        return "Period: " + period + ", path: " + pathId + ", field: " + fieldId + ", collector: " + collectorId;
    }
}
