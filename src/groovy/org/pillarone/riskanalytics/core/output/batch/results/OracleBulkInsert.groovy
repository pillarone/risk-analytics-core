package org.pillarone.riskanalytics.core.output.batch.results

import groovy.sql.Sql


class OracleBulkInsert extends AbstractResultsBulkInsert {

    @Override
    protected void save() {
        long time = System.currentTimeMillis()
        Sql sql = new Sql(simulationRun.dataSource)

        String directoryName = "simulation_${simulationRun.id}"
        String externalTableName = "temp_result_${simulationRun.id}"
        try {
            String createDirectory = "CREATE OR REPLACE DIRECTORY ${directoryName} as '${tempFile.getParent()}'"
            String createExternalTable = "CREATE TABLE ${externalTableName} ( " +
                    "run_id NUMBER(19), period NUMBER(19), iteration NUMBER(19), path_id NUMBER(19), " +
                    "field_id NUMBER(19), collector_id NUMBER(19), value FLOAT(126), value_index NUMBER(19), " +
                    "datetime NUMBER(19) ) ORGANIZATION EXTERNAL ( DEFAULT DIRECTORY ${directoryName} " +
                    "ACCESS PARAMETERS ( RECORDS DELIMITED BY newline FIELDS TERMINATED BY ',' ) LOCATION ('${tempFile.name}') )"
            String insert = "INSERT INTO single_value_result " +
                    "(id, version, simulation_run_id, period, iteration, path_id, collector_id, field_id, value_index, value, date_time) " +
                    "(SELECT result_id_sequence.nextval, 0, run_id, period, iteration, path_id, collector_id, field_id, value_index, value, datetime FROM ${externalTableName})"
            sql.execute(createDirectory)
            sql.execute(createExternalTable)
            int numberOfResults = sql.executeUpdate(insert)
            time = System.currentTimeMillis() - time
            LOG.info("${numberOfResults} results saved in ${time} ms");

        } finally {
            String dropDirectory = "drop directory ${directoryName}"
            String dropExternalTable = "drop table ${externalTableName}"
            sql.execute(dropExternalTable)
            sql.execute(dropDirectory)
            sql.close()
        }

    }

    @Override
    protected void writeResult(List values) {
        writer.append(values.collect { it == null ? "" : it}.join(","))
        writer.newLine()
    }

}
