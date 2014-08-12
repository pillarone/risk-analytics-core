package org.pillarone.riskanalytics.core.upload

import org.pillarone.riskanalytics.core.queue.AbstractQueueService
import org.pillarone.riskanalytics.core.queue.IQueueTaskFuture
import org.pillarone.riskanalytics.core.simulation.SimulationState
import org.pillarone.riskanalytics.core.simulation.item.Simulation

import static com.google.common.base.Preconditions.checkArgument
import static com.google.common.base.Preconditions.checkNotNull

class UploadQueueService extends AbstractQueueService<UploadConfiguration, UploadQueueEntry> {

    IUploadStrategy uploadStrategy

    @Override
    UploadQueueEntry createQueueEntry(UploadConfiguration configuration, int priority) {
        new UploadQueueEntry(configuration, priority)
    }

    @Override
    UploadQueueEntry createQueueEntry(UUID id) {
        return new UploadQueueEntry(id)
    }

    @Override
    IQueueTaskFuture doWork(UploadQueueEntry entry, int priority) {
        uploadStrategy.upload(entry.context, priority)
    }

    @Override
    void handleEntry(UploadQueueEntry entry) {
        if (!entry.context) {
            throw new IllegalStateException("queue task finished without result")
        }
    }

    @Override
    void preConditionCheck(UploadConfiguration configuration) {
        checkNotNull(configuration)
        Simulation simulation = configuration.simulation
        checkNotNull(simulation)
        checkNotNull(simulation.id)
        checkNotNull(simulation.start)
        checkNotNull(simulation.end)
        checkNotNull(simulation.template)
        checkArgument(simulation.simulationState == SimulationState.FINISHED)
    }
}




