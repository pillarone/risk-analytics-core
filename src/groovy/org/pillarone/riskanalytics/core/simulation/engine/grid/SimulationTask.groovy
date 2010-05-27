package org.pillarone.riskanalytics.core.simulation.engine.grid

import org.gridgain.grid.GridTaskSplitAdapter
import org.pillarone.riskanalytics.core.simulation.engine.SimulationConfiguration
import org.gridgain.grid.GridJob
import org.gridgain.grid.GridJobResult


class SimulationTask extends GridTaskSplitAdapter<SimulationConfiguration, Boolean> {


    protected Collection<? extends GridJob> split(int gridSize, SimulationConfiguration simulationConfiguration) {
        int jobSize = simulationConfiguration.simulation.numberOfIterations / gridSize
        simulationConfiguration.simulation.numberOfIterations = jobSize
        def jobs = []
        gridSize.times {
            jobs << new SimulationJob(simulationConfiguration)
        }
        return jobs;
    }

    Boolean reduce(List<GridJobResult> gridJobResults) {
        return null;
    }
}
