package org.pillarone.riskanalytics.core.output.batch.results

import groovy.sql.Sql
import groovy.transform.CompileStatic

@CompileStatic
class SQLServerBulkInsert extends AbstractResultsBulkInsert {

    protected void save() {
        File formatFile = new File(getClass().getResource("/single_value_result_formatfile.xml").toURI())
        long time = System.currentTimeMillis()
        Sql sql = new Sql(simulationRun.dataSource)
        String query = "BULK INSERT single_value_result FROM '${tempFile.getAbsolutePath()}' WITH (FORMATFILE = '${formatFile.getAbsolutePath()}')"
        int numberOfResults = sql.executeUpdate(query.replaceAll('\\\\', '/'))
        time = System.currentTimeMillis() - time
        LOG.info("${numberOfResults} results saved in ${time} ms");
        sql.close()
    }

    protected void writeResult(List values) {
        values.add(0, 0) //dummy id
        values.add(0, 0) //version
        writer.writeLine(values.join(","))
    }


}