package org.pillarone.riskanalytics.core.dataaccess;

public class ResultDescriptor {

    private long pathId;
    private long fieldId;
    private long collectorId;
    private int periodIndex;


    public ResultDescriptor(long pathId, long fieldId, long collectorId, int periodIndex) {
        this.pathId = pathId;
        this.fieldId = fieldId;
        this.collectorId = collectorId;
        this.periodIndex = periodIndex;
    }

    public long getPathId() {
        return pathId;
    }

    public long getFieldId() {
        return fieldId;
    }

    public int getPeriodIndex() {
        return periodIndex;
    }

    public long getCollectorId() {
        return collectorId;
    }
}
