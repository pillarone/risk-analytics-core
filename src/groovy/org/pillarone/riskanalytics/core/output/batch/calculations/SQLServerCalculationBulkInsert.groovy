package org.pillarone.riskanalytics.core.output.batch.calculations

import groovy.sql.Sql
import groovy.transform.CompileStatic

/**
 * Allianz Risk Transfer  ATOM
 * User: bzetterstrom
 */
@CompileStatic
class SQLServerCalculationBulkInsert extends AbstractCalculationsBulkInsert {

    @Override
    protected void save() {
        File formatFile = new File(getClass().getResource("/post_simulation_calculation_formatfile.xml").toURI())
        long time = System.currentTimeMillis()
        Sql sql = new Sql(simulationRun.dataSource)
        String query = "BULK INSERT post_simulation_calculation FROM '${tempFile.getAbsolutePath()}' WITH (FORMATFILE = '${formatFile.getAbsolutePath()}', KEEPNULLS)"
        int numberOfResults = sql.executeUpdate(query.replaceAll('\\\\', '/'))
        time = System.currentTimeMillis() - time
        LOG.info("${numberOfResults} post_simulation_calculation rows saved in ${time} ms");
        sql.close()
    }

    @Override
    protected void writeResult(final List values) {
        values.add(0, 0) //dummy id
        values.add(0, 0) //version
        writer.writeLine(values.join(","))
    }

    @Override
    protected String getNull() {
        return ""; // empty value should mean NULL when the flag WITH(KEEPNULLS) is used
    }
}
