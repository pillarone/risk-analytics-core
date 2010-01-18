package org.pillarone.riskanalytics.core.output.batch

import groovy.sql.Sql
import org.pillarone.riskanalytics.core.output.batch.AbstractBulkInsert

class MysqlBulkInsert extends AbstractBulkInsert {

    protected void writeResult(List values) {
        writer.append(values.join(","))
        writer.append(";")
    }



    void save() {
        long time = System.currentTimeMillis()
        Sql sql = new Sql(simulationRun.dataSource)
        String query = "LOAD DATA INFILE '${tempFile.getAbsolutePath()}' INTO TABLE single_value_result FIELDS TERMINATED BY ',' LINES TERMINATED BY ';' (simulation_run_id, period, iteration, path_id, field_id, collector_id, value)"
        int numberOfResults = sql.executeUpdate(query.replaceAll('\\\\', '/'))
        time = System.currentTimeMillis() - time
        LOG.info("${numberOfResults} results saved in ${time} ms");
        sql.close()
    }
}
