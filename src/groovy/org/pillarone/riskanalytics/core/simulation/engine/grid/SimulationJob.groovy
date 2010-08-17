package org.pillarone.riskanalytics.core.simulation.engine.grid

import org.gridgain.grid.GridJobAdapter
import org.pillarone.riskanalytics.core.simulation.engine.SimulationRunner
import org.pillarone.riskanalytics.core.simulation.engine.SimulationConfiguration
import org.gridgain.grid.GridNode
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import java.text.SimpleDateFormat
import org.pillarone.riskanalytics.core.util.MathUtils

class SimulationJob extends GridJobAdapter<Boolean> {

    private SimulationConfiguration simulationConfiguration
    private GridNode master
    private ArrayList<SimulationBlock> simBlocks;


    private static Log LOG = LogFactory.getLog(SimulationJob)

    public SimulationJob(SimulationConfiguration simulationConfiguration, GridNode gridNode) {
        this.master = gridNode
        this.simulationConfiguration = simulationConfiguration
        //this.simulationConfiguration.outputStrategy = new GridOutputStrategy(gridNode, simulationConfiguration.simulation.id);
        this.simulationConfiguration.outputStrategy = new FileOutputStrategy(gridNode);
        simBlocks=new ArrayList<SimulationBlock>();
    }

    Serializable execute() {
        String start = new SimpleDateFormat("HH:mm:ss").format(new Date())
        ExpandoMetaClass.enableGlobally()
        SimulationRunner runner = SimulationRunner.createRunner()
        runner.setSimulationConfiguration(simulationConfiguration)
        runner.setSimBlocks(simBlocks);
        runner.start()

        return start + " until " + new SimpleDateFormat("HH:mm:ss").format(new Date())
    }

    public void addSimulationBlock (SimulationBlock simBlock){
        simBlocks.add(simBlock);
    }
}
