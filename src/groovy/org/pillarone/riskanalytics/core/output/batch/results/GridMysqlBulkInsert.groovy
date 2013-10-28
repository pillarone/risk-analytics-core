package org.pillarone.riskanalytics.core.output.batch.results

class GridMysqlBulkInsert extends MysqlBulkInsert {

    public synchronized void writeResult(String intermediateResult) {
        writer.append(intermediateResult);
    }
}
