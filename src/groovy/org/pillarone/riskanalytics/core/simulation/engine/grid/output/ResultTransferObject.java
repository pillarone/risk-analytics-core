package org.pillarone.riskanalytics.core.simulation.engine.grid.output;

import java.io.Serializable;

public class ResultTransferObject implements Serializable {

    private ResultDescriptor resultDescriptor;
    private byte[] data;

    public ResultTransferObject(ResultDescriptor resultDescriptor, byte[] data) {
        this.data = data;
        this.resultDescriptor = resultDescriptor;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public ResultDescriptor getResultDescriptor() {
        return resultDescriptor;
    }

    public void setResultDescriptor(ResultDescriptor resultDescriptor) {
        this.resultDescriptor = resultDescriptor;
    }

}
