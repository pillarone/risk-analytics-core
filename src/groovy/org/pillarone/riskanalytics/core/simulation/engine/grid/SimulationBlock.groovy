package org.pillarone.riskanalytics.core.simulation.engine.grid

import groovy.transform.CompileStatic

@CompileStatic
class SimulationBlock implements Serializable {

    int iterationOffset, blockSize, streamOffset;

    public SimulationBlock(int iterationOffset, int blockSize, int streamOffset) {
        this.iterationOffset = iterationOffset;
        this.blockSize = blockSize;
        this.streamOffset = streamOffset;
    }

    String toString() {
        return "First iteration: $iterationOffset, last iteration: ${iterationOffset + blockSize - 1}"
    }


}
