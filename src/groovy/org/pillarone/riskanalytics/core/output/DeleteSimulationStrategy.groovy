package org.pillarone.riskanalytics.core.output

import org.codehaus.groovy.grails.commons.ApplicationHolder
import javax.sql.DataSource


public abstract class DeleteSimulationStrategy {

    abstract void deleteSimulation(SimulationRun simulationRun)

    public static DeleteSimulationStrategy getInstance() {
        DataSource dataSource = ApplicationHolder.application.mainContext.getBean("dataSource")
        return dataSource.url.contains("mysql") ? new MysqlDeleteStrategy() : new DelayedDeleteStrategy()
    }
}