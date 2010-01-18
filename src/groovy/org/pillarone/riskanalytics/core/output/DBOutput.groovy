package org.pillarone.riskanalytics.core.output

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.pillarone.riskanalytics.core.output.batch.AbstractBulkInsert

public class DBOutput implements ICollectorOutputStrategy {

    private static final Log LOG = LogFactory.getLog(ICollectorOutputStrategy)

    public AbstractBulkInsert batchInsert

    public DBOutput() {
        this.batchInsert = AbstractBulkInsert.getBulkInsertInstance()
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
