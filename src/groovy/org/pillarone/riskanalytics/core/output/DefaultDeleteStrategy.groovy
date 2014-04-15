package org.pillarone.riskanalytics.core.output

import org.pillarone.riskanalytics.core.BatchRun
import org.pillarone.riskanalytics.core.simulation.engine.grid.GridHelper

class DefaultDeleteStrategy extends DeleteSimulationStrategy {

    void deleteSimulation(SimulationRun simulationRun) {
        new File(GridHelper.getResultLocation(simulationRun.id)).deleteDir()
        SimulationRun.withTransaction {
            PostSimulationCalculation.findAllByRun(simulationRun)*.delete() // there are only few of them...
            SingleValueResult.executeUpdate("delete from $SingleValueResult.name where simulationRun = ?", [simulationRun])
            BatchRun batchRun = simulationRun.batchRun
            if (batchRun) {
                batchRun.removeFromSimulationRuns(simulationRun)
                batchRun.save(flush: true)
            } else {
                simulationRun.delete(flush: true)
            }
        }
    }
}
