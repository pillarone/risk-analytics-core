package org.pillarone.riskanalytics.core

import org.junit.Test
import org.pillarone.riskanalytics.core.batch.BatchRunService
import org.pillarone.riskanalytics.core.simulation.item.Batch

import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertNull

class BatchRunTests {

    BatchRunService batchRunService

    @Test
    public void testDeleteBatchRun() {
        BatchRun batchRun = new BatchRun()
        batchRun.name = "TestDeleteMe"
        batchRun.save()
        BatchRun bRun = BatchRun.findByName(batchRun.name)
        assertNotNull bRun

        Batch batch = new Batch(batchRun.name)

        batchRunService.deleteBatch(batch)

        bRun = BatchRun.findByName(batch.name)
        assertNull bRun
    }
}
