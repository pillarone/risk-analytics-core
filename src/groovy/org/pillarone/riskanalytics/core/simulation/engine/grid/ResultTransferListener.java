package org.pillarone.riskanalytics.core.simulation.engine.grid;

import org.gridgain.grid.GridException;
import org.gridgain.grid.GridListenActor;
import org.pillarone.riskanalytics.core.simulation.SimulationState;
import org.pillarone.riskanalytics.core.simulation.engine.grid.output.ResultTransferObject;

import java.util.UUID;


public class ResultTransferListener extends GridListenActor<ResultTransferObject> {

    private SimulationTask simulationTask;

    public ResultTransferListener(SimulationTask simulationTask) {
        this.simulationTask = simulationTask;
    }

    @Override
    public void receive(UUID nodeId, ResultTransferObject msg) throws GridException {
        try {
            simulationTask.onMessage(msg);
        } catch (Exception e) {
            simulationTask.getSimulationErrors().add(e);
            simulationTask.setSimulationState(SimulationState.ERROR);
            throw new RuntimeException(e);
        }
    }

    public void removeListener() {
        stop();
    }
}
