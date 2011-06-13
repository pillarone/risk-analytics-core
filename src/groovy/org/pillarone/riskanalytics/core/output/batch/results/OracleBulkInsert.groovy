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
            String createDirectory = "create or replace directory ${directoryName} as '${tempFile.getParent()}'"
            String createExternalTable = "create table ${externalTableName} ( run_id number(19), period number(19), iteration number(19), path_id number(19), field_id number(19), collector_id number(19), value float(126), value_index number(19), datetime number(19) ) organization external ( default directory ${directoryName} access parameters ( records delimited by newline fields terminated by ',' ) location ('${tempFile.name}') )"
            String insert = "insert into single_value_result (id, version, simulation_run_id, period, iteration, path_id, collector_id, field_id, value_index, value, date_time) (select result_id_sequence.nextval, 0, run_id, period, iteration, path_id, collector_id, field_id, value_index, value, datetime from ${externalTableName})"
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
        //TODO: properly handle null dates
        writer.append(values.collect { it == null ? 0 : it}.join(","))
        writer.newLine()
    }

}
