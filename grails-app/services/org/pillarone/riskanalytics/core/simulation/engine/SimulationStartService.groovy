package org.pillarone.riskanalytics.core.simulation.engine

import org.gridgain.grid.Grid
import org.gridgain.grid.GridTaskFuture
import org.pillarone.riskanalytics.core.output.SimulationRun
import org.pillarone.riskanalytics.core.simulation.engine.grid.SpringBeanDefinitionRegistry

class SimulationStartService {

    Grid grid
    def backgroundService

    /**
     *
     * @param queueEntry the queueEntry contains all information to run a simulation. As the name suggest this method should only be called by the simulation queue.
     * But it is possible to bypass the simulation queue and start directly a task in the grid.
     * @param futureCallback after calling the grid this callback is called with a gridTaskFuture and the queueEntry. This gives you a handle to the running simulation.
     */
    void start(QueueEntry queueEntry, Closure futureCallback) {
        backgroundService.execute("start grid task") {
            SimulationConfiguration configuration = queueEntry.simulationConfiguration
            SimulationRun.withTransaction {
                configuration.createMappingCache(configuration.simulation.template)
            }
            configuration.prepareSimulationForGrid()
            configuration.beans = SpringBeanDefinitionRegistry.requiredBeanDefinitions
            GridTaskFuture gridTaskFuture = grid.execute(queueEntry.simulationTask, queueEntry.simulationConfiguration)
            futureCallback.call(gridTaskFuture, queueEntry)
        }
    }
}
