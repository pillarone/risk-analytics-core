package org.pillarone.riskanalytics.core.simulation.engine.grid.output;

import java.io.Serializable;

public class ResultTransferObject implements Serializable {

    private ResultDescriptor resultDescriptor;
    private byte[] data;
    private int progress;

    public ResultTransferObject(ResultDescriptor resultDescriptor, byte[] data, int progress) {
        this.data = data;
        this.resultDescriptor = resultDescriptor;
        this.progress = progress;
    }

    public byte[] getData() {
        return data;
    }

    public ResultDescriptor getResultDescriptor() {
        return resultDescriptor;
    }

    public int getProgress() {
        return progress;
    }
}
