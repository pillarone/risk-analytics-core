package org.pillarone.riskanalytics.core.simulation.engine

import org.pillarone.riskanalytics.core.BatchRun
import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.batch.BatchRunService
import org.pillarone.riskanalytics.core.output.OutputStrategy


abstract class BatchRunTest extends ModelTest {

    @Override
    protected void setUp() {
        super.setUp()

    }

    final void testBatchRun() {
        BatchRun batchRun = new BatchRun(name: "testBatchRun", executionTime: new DateTime(2100, 1, 1, 0, 0, 0, 0)).save()
        assertNotNull batchRun

        BatchRunService service = BatchRunService.service
        service.addSimulationRun(batchRun, run, OutputStrategy.FILE_OUTPUT)
        service.runBatch(batchRun)

        int totalWait = 0

        while (!batchRun.executed) {
            sleep 2000
            totalWait += 2000
            if (totalWait > 60000) {
                throw new RuntimeException("Batch run did not finish.")
            }
            batchRun.refresh()
        }

        service.runnerRegistry.stop()

    }
}
