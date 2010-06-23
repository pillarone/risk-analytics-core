package org.pillarone.riskanalytics.core.output

import org.apache.commons.logging.LogFactory
import org.apache.commons.logging.Log


class DelayedDeleteStrategy extends DeleteSimulationStrategy {

    private static Log LOG = LogFactory.getLog(DelayedDeleteStrategy)

    void deleteSimulation(SimulationRun simulationRun) {
        simulationRun.deleteSimulationService.deleteSimulation(simulationRun)
        LOG.info "Simulation ${simulationRun.name} is marked as deleted"
    }


}
