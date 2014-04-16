package org.pillarone.riskanalytics.core.simulation.engine

import org.junit.Test
import org.pillarone.riskanalytics.core.BatchRun
import org.pillarone.riskanalytics.core.batch.BatchRunService
import org.pillarone.riskanalytics.core.simulation.item.Batch

import static org.junit.Assert.assertNotNull

abstract class BatchRunTest extends ModelTest {

    @Test
    final void testBatchRun() {
        BatchRunService service = BatchRunService.service
        BatchRun batchRun = null
        BatchRun.withNewSession { def session ->
            batchRun = new BatchRun(name: "testBatchRun").save(flush: true)
            assertNotNull batchRun
            service.createBatchRunSimulationRun(batchRun, run)
            session.flush()
        }
        Batch batch = new Batch(batchRun.name)
        service.runBatch(batch)
        sleep(5000)
        int totalWait = 0
        while (!isBatchRunExecuted(batchRun)) {
            sleep 2000
            totalWait += 2000
            if (totalWait > 60000) {
                throw new RuntimeException("Batch run did not finish.")
            }
            batchRun.refresh()
        }
    }

    private boolean isBatchRunExecuted(BatchRun batchRun) {
        BatchRun.createCriteria().get {
            eq('id', batchRun.id)
            projections {
                property('executed')
            }
        }
    }
}
