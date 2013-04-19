package org.pillarone.riskanalytics.core.output

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.pillarone.riskanalytics.core.output.batch.results.AbstractResultsBulkInsert

/**
 * This implementation of ICollectorOutputStrategy must be used in test context only. It's required in the KTI ctx as
 * kti collecting strategies don't memorize the simulation run reference but we need it for more easy testing of specific
 * paths, fields and values.
 */
public class TestDBOutput implements ICollectorOutputStrategy {

    private static final Log LOG = LogFactory.getLog(ICollectorOutputStrategy)

    public AbstractResultsBulkInsert batchInsert

    public TestDBOutput() {
        this.batchInsert = AbstractResultsBulkInsert.getBulkInsertInstance()
    }

    public ICollectorOutputStrategy leftShift(List<SingleValueResultPOJO> results) {

        if (batchInsert) {
            if (!batchInsert.isInitialized()) {
                batchInsert.simulationRun = SimulationRun.list()[0]
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
