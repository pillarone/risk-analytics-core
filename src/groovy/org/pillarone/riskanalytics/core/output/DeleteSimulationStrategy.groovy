package org.pillarone.riskanalytics.core.output

import grails.util.Environment
import groovy.transform.CompileStatic


@CompileStatic
public abstract class DeleteSimulationStrategy {

    abstract void deleteSimulation(SimulationRun simulationRun)

    public static DeleteSimulationStrategy getInstance() {
        return Environment.current.getName().contains("mysql") ? new MysqlDeleteStrategy() : new DelayedDeleteStrategy()
    }
}