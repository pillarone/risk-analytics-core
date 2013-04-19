package org.pillarone.riskanalytics.core.simulation.engine

import grails.util.Holders
import groovy.transform.CompileStatic
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.gridgain.grid.Grid

import org.pillarone.riskanalytics.core.simulation.engine.grid.SimulationTask

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.pillarone.riskanalytics.core.simulation.engine.grid.SimulationHandler
import org.pillarone.riskanalytics.core.simulation.item.ResultConfiguration
import org.pillarone.riskanalytics.core.simulation.engine.grid.SpringBeanDefinitionRegistry

public class RunSimulationService {

    private static Log LOG = LogFactory.getLog(RunSimulationService)

    def backgroundService
    Grid grid

    @CompileStatic
    public static RunSimulationService getService() {
        return Holders.applicationContext.getBean(RunSimulationService)
    }

    /**
     * Runs a simulation asynchronously using the background thread plugin.
     * @param runner
     *          A simulation runner which already has its pre and post simulation actions configured.
     * @param configuration
     *          A simulation configuration which defines the simulation run and output strategy
     */
    public synchronized SimulationRunner runSimulation(SimulationRunner runner, SimulationConfiguration configuration) {
        backgroundService.execute(configuration.simulation.name) { //don't start a transaction here, but inside SimulationRunner (problems with certain dbs.)
            runner.simulationConfiguration = configuration
            runner.start()
        }

        return runner
    }

    /**
     * Runs a simulation on a GridGain grid
     * @param configuration the simulation details
     * @return the result of the grid gain task
     */
    @CompileStatic
    public SimulationHandler runSimulationOnGrid(SimulationConfiguration configuration, ResultConfiguration resultConfiguration) {
        configuration.createMappingCache(resultConfiguration)
        configuration.prepareSimulationForGrid()
        configuration.beans = SpringBeanDefinitionRegistry.getRequiredBeanDefinitions()

        SimulationTask task = new SimulationTask()
        SimulationHandler handler = new SimulationHandler(simulationTask: task)
        handler.gridTaskFuture = grid.execute(task, configuration)

        return handler
    }


}