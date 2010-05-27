package org.pillarone.riskanalytics.core.simulation.engine.grid

import org.gridgain.grid.GridJobAdapter
import org.pillarone.riskanalytics.core.simulation.engine.SimulationRunner
import org.pillarone.riskanalytics.core.simulation.engine.SimulationConfiguration

class SimulationJob extends GridJobAdapter<Boolean> {

    private SimulationConfiguration simulationConfiguration

    public SimulationJob(SimulationConfiguration simulationConfiguration) {
        this.simulationConfiguration = simulationConfiguration
        this.simulationConfiguration.outputStrategy = new GridOutputStrategy()
    }

    Serializable execute() {
        SimulationRunner runner = SimulationRunner.createRunner()
        runner.setSimulationConfiguration(simulationConfiguration)
        runner.start()

        return runner.error == null
    }
}
