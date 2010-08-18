package org.pillarone.riskanalytics.core.simulation.engine.grid

class SimulationBlock implements Serializable {

    int iterationOffset, blockSize, streamOffset;

    public SimulationBlock(int iterationOffset, int blockSize, int streamOffset) {
        this.iterationOffset = iterationOffset;
        this.blockSize = blockSize;
        this.streamOffset = streamOffset;
    }
}
