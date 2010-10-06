package org.pillarone.riskanalytics.core.output

import grails.util.Environment


public abstract class DeleteSimulationStrategy {

    abstract void deleteSimulation(SimulationRun simulationRun)

    public static DeleteSimulationStrategy getInstance() {
        return Environment.current.name().contains("mysql") ? new MysqlDeleteStrategy() : new DelayedDeleteStrategy()
    }
}