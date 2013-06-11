package org.pillarone.riskanalytics.core.output.batch.calculations

import groovy.sql.Sql
import groovy.transform.CompileStatic
import org.pillarone.riskanalytics.core.output.SimulationRun
import java.sql.SQLException

@CompileStatic
class MysqlCalculationsBulkInsert extends AbstractCalculationsBulkInsert {

    protected void writeResult(List values) {
        writer.append(values.join(","))
        writer.append(";")
    }

    @Override
    void setSimulationRun(SimulationRun simulationRun) {
        super.setSimulationRun(simulationRun)
        Sql sql = new Sql(simulationRun.getDataSource());
        try {
            sql.execute("ALTER TABLE post_simulation_calculation ADD PARTITION (PARTITION P" + getSimulationRunId() + " VALUES IN (" + getSimulationRunId() + "))");
        } catch (Exception ex) {
            deletePartitionIfExist(sql, getSimulationRunId());
            try {
                sql.execute("ALTER TABLE post_simulation_calculation ADD PARTITION (PARTITION P" + getSimulationRunId() + " VALUES IN (" + getSimulationRunId() + "))");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void deletePartitionIfExist(Sql sql, long partitionName) {
        try {
            sql.execute("ALTER TABLE post_simulation_calculation DROP PARTITION P" + partitionName);
        } catch (Exception e) {//the partition was not created yet
        }
    }

    protected void save() {
        long time = System.currentTimeMillis()
        Sql sql = new Sql(simulationRun.dataSource)
        String query = "LOAD DATA LOCAL INFILE '${tempFile.getAbsolutePath()}' INTO TABLE post_simulation_calculation FIELDS TERMINATED BY ',' LINES TERMINATED BY ';' (run_id, period, path_id, field_id, collector_id, key_figure, key_figure_parameter, result)"
        int numberOfResults = sql.executeUpdate(query.replaceAll('\\\\', '/'))
        time = System.currentTimeMillis() - time
        LOG.info("${numberOfResults} results saved in ${time} ms");
        sql.close()
    }

    protected String getNull() {
        return "\\N"
    }

}
