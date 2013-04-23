package org.pillarone.riskanalytics.core.output

import grails.util.Holders
import groovy.transform.CompileStatic
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.hibernate.SessionFactory
import org.pillarone.riskanalytics.core.BatchRunSimulationRun

public class DeleteSimulationService {

    static transactional = true

    SessionFactory sessionFactory

    private static Log LOG = LogFactory.getLog(DeleteSimulationService)

    /**
     * Marks a simulation run as to be deleted. Simulations are not deleted right away because
     * it might take a while if there are many results.
     * Parameterization and result configuration are set to null in order not to block them
     * from editing if this was the only simulation that used them.
     */
    Object deleteSimulation(SimulationRun simulationRun) {
        simulationRun.withTransaction {
            simulationRun.name = simulationRun.name + "TOBEDELETED" + System.currentTimeMillis()
            simulationRun.toBeDeleted = true
            simulationRun.parameterization = null
            simulationRun.resultConfiguration = null
            if (!simulationRun.save(flush: true)) {
                simulationRun.errors.each { LOG.error it }
            }
        }
        return simulationRun
    }

    void deleteAllMarkedSimulations() {  // todo (dk): wait until no simulation is running
        SimulationRun.withTransaction {
            SimulationRun.findAllByToBeDeleted(true).each {SimulationRun simulationRun ->
                deleteBatchRunSimulationRun(simulationRun)
                PostSimulationCalculation.findAllByRun(simulationRun)*.delete() // there are only few of them...
                SingleValueResult.executeUpdate("delete from $SingleValueResult.name where simulationRun = ?", [simulationRun])
                simulationRun.delete(flush: true)
            }
        }
    }

    private void deleteBatchRunSimulationRun(SimulationRun simulationRun) {
        BatchRunSimulationRun.findBySimulationRun(simulationRun).each {BatchRunSimulationRun batchRunSimulationRun ->
            batchRunSimulationRun.delete()
        }
    }

    /**
     * Returns the singleton spring bean from the application context.
     */
    @CompileStatic
    static DeleteSimulationService getInstance() {
        return Holders.applicationContext.getBean(DeleteSimulationService)
    }
}