package org.pillarone.riskanalytics.core.simulation.engine.grid.output;

import java.io.Serializable;
import java.util.UUID;

public class ResultTransferObject implements Serializable {

    private ResultDescriptor resultDescriptor;
    private byte[] data;
    private int progress;
    private UUID jobIdentifier;

    public ResultTransferObject(ResultDescriptor resultDescriptor, UUID id, byte[] data, int progress) {
        this.data = data;
        this.resultDescriptor = resultDescriptor;
        this.progress = progress;
        this.jobIdentifier = id;
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

    public UUID getJobIdentifier() {
        return jobIdentifier;
    }
}
