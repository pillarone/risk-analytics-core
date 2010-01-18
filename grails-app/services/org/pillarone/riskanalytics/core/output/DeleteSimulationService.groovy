package org.pillarone.riskanalytics.core.output

import org.hibernate.SessionFactory

public class DeleteSimulationService {
    static transactional = true
    SessionFactory sessionFactory

    Object deleteSimulation(SimulationRun simulationRun) {
        simulationRun.withTransaction {
            simulationRun.name = simulationRun.name + "TOBEDELETED" + System.currentTimeMillis()
            simulationRun.toBeDeleted = true
            simulationRun.parameterization = null
            simulationRun.resultConfiguration = null
            simulationRun.save(flush: true)
            if (!simulationRun) {
                simulationRun.errors.each { log.error it }
            }
        }
        return simulationRun
    }

    void deleteAllMarkedSimulations() {  // todo (dk): wait until no simulation is running
        SimulationRun.withTransaction {
            SimulationRun.findAllByToBeDeleted(true).each {SimulationRun simulationRun ->
                PostSimulationCalculation.findAllByRun(simulationRun)*.delete() // there are only few of them...
                SingleValueResult.executeUpdate("delete from $SingleValueResult.name where simulationRun = ?", [simulationRun])
                simulationRun.delete(flush: true)
            }
        }
    }
}