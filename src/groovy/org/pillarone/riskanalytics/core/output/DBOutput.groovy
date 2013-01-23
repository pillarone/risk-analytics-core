package org.pillarone.riskanalytics.core.output

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.pillarone.riskanalytics.core.output.batch.AbstractBulkInsert
import org.pillarone.riskanalytics.core.output.batch.results.AbstractResultsBulkInsert

public class DBOutput implements ICollectorOutputStrategy {

    private static final Log LOG = LogFactory.getLog(ICollectorOutputStrategy)

    public AbstractResultsBulkInsert batchInsert

    public DBOutput() {
        this.batchInsert = AbstractResultsBulkInsert.getBulkInsertInstance()
    }

    public ICollectorOutputStrategy leftShift(List results) {

        if (batchInsert) {
            if (!batchInsert.isInitialized()) {
                batchInsert.setSimulationRun(results[0].simulationRun)
            }
            batchInsert.addResults(results)
        }



        return this
    }

    public void finish() {
        batchInsert.saveToDB()
        batchInsert.reset()
    }

}
