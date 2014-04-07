package org.pillarone.riskanalytics.core.output

import org.pillarone.riskanalytics.core.BatchRunSimulationRun
import org.pillarone.riskanalytics.core.simulation.engine.grid.GridHelper

class DefaultDeleteStrategy extends DeleteSimulationStrategy {

    void deleteSimulation(SimulationRun simulationRun) {
        new File(GridHelper.getResultLocation(simulationRun.id)).deleteDir()
        SimulationRun.withTransaction {
            deleteBatchRunSimulationRun(simulationRun)
            PostSimulationCalculation.findAllByRun(simulationRun)*.delete() // there are only few of them...
            SingleValueResult.executeUpdate("delete from $SingleValueResult.name where simulationRun = ?", [simulationRun])
            simulationRun.delete(flush: true)
        }
    }

    private void deleteBatchRunSimulationRun(SimulationRun simulationRun) {
        BatchRunSimulationRun.findBySimulationRun(simulationRun).each { BatchRunSimulationRun batchRunSimulationRun ->
            batchRunSimulationRun.delete()
        }
    }
}
