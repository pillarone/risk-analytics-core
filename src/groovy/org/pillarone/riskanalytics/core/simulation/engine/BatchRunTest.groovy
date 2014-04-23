package org.pillarone.riskanalytics.core.simulation.engine

import grails.util.Holders
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.pillarone.riskanalytics.core.batch.BatchRunService
import org.pillarone.riskanalytics.core.simulation.item.Batch

import java.util.concurrent.CountDownLatch

abstract class BatchRunTest extends ModelTest {

    MyListener listener

    @Before
    void addListener() {
        listener = new MyListener()
        simulationQueueService.addSimulationQueueListener(listener)
    }

    @After
    void removeListener() {
        simulationQueueService.removeSimulationQueueListener(listener)
        listener = null
    }

    @Test
    final void testBatchRun() {
        Batch batch = new Batch("testBatchRun")
        batch.simulationProfileName = simulationProfile.name
        batch.parameterizations = [run.parameterization]
        assert batch.save()
        batchRunService.runBatch(batch)
        assert batch.executed
        assert listener.offered.size() == 1
        QueueEntry entry = listener.offered.first()
        assert entry.simulationConfiguration.simulation.parameterization == run.parameterization
        //wait to finish simulation
        listener.waitUntilFinished()
    }

    static BatchRunService getBatchRunService() {
        Holders.grailsApplication.mainContext.getBean('batchRunService', BatchRunService)
    }

    static SimulationQueueService getSimulationQueueService() {
        Holders.grailsApplication.mainContext.getBean('simulationQueueService', SimulationQueueService)
    }


    static class MyListener implements ISimulationQueueListener {
        CountDownLatch latch = new CountDownLatch(1)

        List<QueueEntry> offered = []

        void waitUntilFinished() {
            latch.await()
        }

        @Override
        void starting(QueueEntry entry) {}

        @Override
        void finished(UUID id) {
            latch.countDown()
        }

        @Override
        void removed(UUID id) {}

        @Override
        void offered(QueueEntry entry) {
            offered << entry
        }
    }
}
