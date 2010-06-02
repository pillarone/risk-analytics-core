package org.pillarone.riskanalytics.core.simulation.engine.grid

import org.gridgain.grid.GridTaskSplitAdapter
import org.pillarone.riskanalytics.core.simulation.engine.SimulationConfiguration
import org.gridgain.grid.GridJob
import org.gridgain.grid.GridJobResult
import org.gridgain.grid.GridFactory


class SimulationTask extends GridTaskSplitAdapter<SimulationConfiguration, Object> {

    protected static final int CORE_COUNT = 2

    protected Collection<? extends GridJob> split(int gridSize, SimulationConfiguration simulationConfiguration) {
        gridSize *= CORE_COUNT
        int jobSize = simulationConfiguration.simulation.numberOfIterations / gridSize
        simulationConfiguration.simulation.numberOfIterations = jobSize
        def jobs = []
        gridSize.times {
            jobs << new SimulationJob(simulationConfiguration, GridHelper.getGrid().getLocalNode())
        }
        return jobs;
    }

    Object reduce(List<GridJobResult> gridJobResults) {
        List l = []
        for (GridJobResult res in gridJobResults) {
            l << res.getData()
        }

        return l
    }
}
