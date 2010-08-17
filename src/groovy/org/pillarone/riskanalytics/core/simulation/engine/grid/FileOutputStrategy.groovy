package org.pillarone.riskanalytics.core.simulation.engine.grid

import org.pillarone.riskanalytics.core.output.ICollectorOutputStrategy
import org.pillarone.riskanalytics.core.output.SingleValueResultPOJO
import org.gridgain.grid.GridNode
import org.gridgain.grid.Grid
import org.pillarone.riskanalytics.core.simulation.engine.SimulationConfiguration


class FileOutputStrategy implements ICollectorOutputStrategy, Serializable{

    Grid grid
    GridNode node
//    ArrayList<Object[]> resultBuffer = new ArrayList<Object[]>(10000)
    //StringBuilder buffer = new StringBuilder()
    private static final String SEPARATOR=" ";
    private HashMap<String,ByteArrayOutputStream>  iterationStore=new HashMap<String,ByteArrayOutputStream>();

    int resCount = 0
    protected long simulationRunId;

    public FileOutputStrategy(GridNode masterNode) {
        grid = getGrid()
        node = masterNode
    }

    private Grid getGrid() {
        if (grid == null) {
            grid = GridHelper.getGrid()
        }
        return grid
    }

    void finish() {
        sendResults()
    }

    ICollectorOutputStrategy leftShift(List results) {
        for (SingleValueResultPOJO result in results) {
            String fname=result.path.id+"_"+result.period+"_"+result.field.id;
            resCount++;
            ByteArrayOutputStream buffer;
            if ((buffer=iterationStore.get(fname))==null){
                buffer=new ByteArrayOutputStream();
                iterationStore.put(fname,buffer);
            }
            DataOutputStream dos=new DataOutputStream(buffer);
            dos.writeInt(result.iteration);
            dos.writeDouble (result.value);

        }
        if (resCount > 100000) {
            sendResults()
        }
        return this
    }

    protected void sendResults() {
        for (String key:iterationStore.keySet()){
            ByteArrayOutputStream buffer=iterationStore.get(key);
            HashMap<String,byte[]> transfer=new HashMap<String,byte[]>(1);
            transfer.put(key,buffer.toByteArray());
            getGrid().sendMessage(node,transfer);
            buffer.reset();
        }

        resCount = 0
    }
}
