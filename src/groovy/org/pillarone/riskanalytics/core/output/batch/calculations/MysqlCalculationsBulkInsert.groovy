package org.pillarone.riskanalytics.core.output.batch.calculations

import groovy.sql.Sql


class MysqlCalculationsBulkInsert extends AbstractCalculationsBulkInsert {

    protected void writeResult(List values) {
        writer.append(values.join(","))
        writer.append(";")
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
