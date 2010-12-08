package org.pillarone.riskanalytics.core.output.batch.results

import groovy.sql.Sql
import org.pillarone.riskanalytics.core.output.SimulationRun

class MysqlBulkInsert extends AbstractResultsBulkInsert {

    protected void writeResult(List values) {
        writer.append(values.join(","))
        writer.append(";")
    }

    synchronized void setSimulationRun(SimulationRun simulationRun) {
        super.setSimulationRun(simulationRun);
        Sql sql = new Sql(simulationRun.dataSource)
        deletePartitionIfExist(sql, "${simulationRunId}")
        sql.execute("ALTER TABLE single_value_result ADD PARTITION (PARTITION P${simulationRunId} VALUES IN (${simulationRunId}))")
    }



    void save() {
        long time = System.currentTimeMillis()
        Sql sql = new Sql(simulationRun.dataSource)
        String query = "LOAD DATA LOCAL INFILE '${tempFile.getAbsolutePath()}' INTO TABLE single_value_result FIELDS TERMINATED BY ',' LINES TERMINATED BY ';' (simulation_run_id, period, iteration, path_id, field_id, collector_id, value, value_index, date)"
        int numberOfResults = sql.executeUpdate(query.replaceAll('\\\\', '/'))
        time = System.currentTimeMillis() - time
        LOG.info("${numberOfResults} results saved in ${time} ms");
        sql.close()
    }
    /**
     * if the partition exists before, it causes an exception
     * @param sql
     * @param partitionName
     */
    private void deletePartitionIfExist(Sql sql, String partitionName) {
        try {
            Object row = sql.firstRow("SELECT PARTITION_NAME FROM INFORMATION_SCHEMA.PARTITIONS where table_name='single_value_result' and PARTITION_NAME = ${partitionName}")
            if (row != null) {
                sql.execute("ALTER TABLE single_value_result DROP PARTITION ${partitionName}")
            }
        } catch (Exception e) {//the partition was not created yet
        }
    }
}
