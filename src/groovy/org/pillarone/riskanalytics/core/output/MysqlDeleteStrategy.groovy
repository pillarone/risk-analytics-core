package org.pillarone.riskanalytics.core.output

import org.springframework.jdbc.datasource.DataSourceUtils
import groovy.sql.Sql
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory


class MysqlDeleteStrategy extends DeleteSimulationStrategy {

    private static Log LOG = LogFactory.getLog(MysqlDeleteStrategy)

    void deleteSimulation(SimulationRun simulationRun) {
        SimulationRun.withTransaction {
            Sql sql = new Sql(DataSourceUtils.getConnection(simulationRun.dataSource))
            long time = System.currentTimeMillis()
            sql.execute("ALTER TABLE single_value_result DROP PARTITION P${simulationRun.id}")
            sql.execute("DELETE FROM post_simulation_calculation where run_id=${simulationRun.id}")
            simulationRun.delete(flush: true)
            LOG.info "Simulation ${simulationRun.name} deleted in ${System.currentTimeMillis() - time}ms"
        }
    }


}
