package org.pillarone.riskanalytics.core.output

import org.springframework.jdbc.datasource.DataSourceUtils
import groovy.sql.Sql
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.pillarone.riskanalytics.core.BatchRunSimulationRun


class MysqlDeleteStrategy extends DeleteSimulationStrategy {

    private static Log LOG = LogFactory.getLog(MysqlDeleteStrategy)

    void deleteSimulation(SimulationRun simulationRun) {
        SimulationRun.withTransaction {
            Sql sql = new Sql(DataSourceUtils.getConnection(simulationRun.dataSource))
            long time = System.currentTimeMillis()
            try {
                sql.execute("ALTER TABLE single_value_result DROP PARTITION P${simulationRun.id}")
            } catch (Exception e) {
                //the partition was not created yet
            }
            try {
                sql.execute("ALTER TABLE post_simulation_calculation DROP PARTITION P${simulationRun.id}")
            } catch (Exception e) {
//                the partition was not created yet
            }

            sql.execute("DELETE FROM post_simulation_calculation where run_id=${simulationRun.id}")
            BatchRunSimulationRun.findAllBySimulationRun(simulationRun)*.delete()
            simulationRun.delete(flush: true)
            LOG.info "Simulation ${simulationRun.name} deleted in ${System.currentTimeMillis() - time}ms"
        }
    }


}