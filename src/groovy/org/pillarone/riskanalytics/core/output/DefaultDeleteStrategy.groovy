package org.pillarone.riskanalytics.core.output

import org.apache.commons.logging.LogFactory
import org.apache.commons.logging.Log
import org.pillarone.riskanalytics.core.BatchRunSimulationRun
import org.pillarone.riskanalytics.core.simulation.engine.grid.GridHelper

class DefaultDeleteStrategy extends DeleteSimulationStrategy {

    private static Log LOG = LogFactory.getLog(DefaultDeleteStrategy)

    void deleteSimulation(SimulationRun simulationRun) {
        new File(GridHelper.getResultLocation(simulationRun.id)).deleteDir()
        SimulationRun.withTransaction {
            deleteBatchRunSimulationRun(simulationRun)
            PostSimulationCalculation.findAllByRun(simulationRun)*.delete() // there are only few of them...
            SingleValueResult.executeUpdate("delete from $SingleValueResult.name where simulationRun = ?", [simulationRun])
            simulationRun.delete(flush: true)
        }
        LOG.info "Simulation ${simulationRun.name} deleted"
    }

    private void deleteBatchRunSimulationRun(SimulationRun simulationRun) {
        BatchRunSimulationRun.findBySimulationRun(simulationRun).each { BatchRunSimulationRun batchRunSimulationRun ->
            batchRunSimulationRun.delete()
        }
    }


}
