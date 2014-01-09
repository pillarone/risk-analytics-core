package org.pillarone.riskanalytics.core.output

import grails.util.Environment
import groovy.transform.CompileStatic


@CompileStatic
public abstract class DeleteSimulationStrategy {

    abstract void deleteSimulation(SimulationRun simulationRun)

    public static DeleteSimulationStrategy getInstance() {
        // TODO Should become a config parameter.
        return Environment.current.getName().contains("mysql") ? new MysqlDeleteStrategy() : new DefaultDeleteStrategy()
    }
}