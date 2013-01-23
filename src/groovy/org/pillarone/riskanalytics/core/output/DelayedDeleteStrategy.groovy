package org.pillarone.riskanalytics.core.output

import org.apache.commons.logging.LogFactory
import org.apache.commons.logging.Log
import org.pillarone.riskanalytics.core.simulation.engine.grid.GridHelper


class DelayedDeleteStrategy extends DeleteSimulationStrategy {

    private static Log LOG = LogFactory.getLog(DelayedDeleteStrategy)

    void deleteSimulation(SimulationRun simulationRun) {
        new File(GridHelper.getResultLocation(simulationRun.id)).deleteDir()
        DeleteSimulationService.instance.deleteSimulation(simulationRun)
        LOG.info "Simulation ${simulationRun.name} is marked as deleted"
    }


}
