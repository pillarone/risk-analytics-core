package org.pillarone.riskanalytics.core.batch
import grails.util.Holders
import groovy.transform.CompileStatic
import org.pillarone.riskanalytics.core.output.SimulationRun
import org.pillarone.riskanalytics.core.simulation.SimulationState
import org.pillarone.riskanalytics.core.simulation.item.Simulation

class BatchRunInfoService {

    @CompileStatic
    static BatchRunInfoService getService() {
        return Holders.grailsApplication.mainContext.getBean(BatchRunInfoService)
    }


    @CompileStatic
    void batchSimulationStateChanged(Simulation simulation, SimulationState simulationState) {
        SimulationRun.withTransaction {
            SimulationRun lockedSim = SimulationRun.lock(simulation.id)
            if (lockedSim) {
                lockedSim.simulationState = simulationState
                lockedSim.save(flush: true)
            }
        }
    }

    @CompileStatic
    SimulationState getSimulationState(Simulation simulationRun) {
        SimulationRun.get(simulationRun.id)?.simulationState
    }
}

