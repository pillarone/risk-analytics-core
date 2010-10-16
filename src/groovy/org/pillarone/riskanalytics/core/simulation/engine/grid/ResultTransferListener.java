package org.pillarone.riskanalytics.core.simulation.engine.grid;

import org.gridgain.grid.GridException;
import org.gridgain.grid.GridListenActor;
import org.pillarone.riskanalytics.core.simulation.engine.grid.output.ResultTransferObject;

import java.util.UUID;


public class ResultTransferListener extends GridListenActor<ResultTransferObject> {

    SimulationTask simTask;
    public ResultTransferListener(SimulationTask simTask){
        this.simTask=simTask;
    }

    @Override
    public void receive(UUID nodeId, ResultTransferObject msg) throws GridException {
        simTask.onMessage(nodeId,msg);
    }

    public void removeListener(){
        stop();
    }
}
