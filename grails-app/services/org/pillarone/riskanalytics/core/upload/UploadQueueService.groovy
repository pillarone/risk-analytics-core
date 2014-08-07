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
    IQueueTaskFuture doWork(UploadConfiguration configuration, int priority) {
        uploadStrategy.upload(configuration, priority)
    }

    @Override
    void preConditionCheck(UploadConfiguration configuration) {
        UploadConfiguration uploadConfiguration = configuration as UploadConfiguration
        checkNotNull(uploadConfiguration)
        Simulation simulation = uploadConfiguration.simulation
        checkNotNull(simulation)
        checkNotNull(simulation.id)
        checkNotNull(simulation.start)
        checkNotNull(simulation.end)
        checkNotNull(simulation.template)
        checkArgument(simulation.simulationState == SimulationState.FINISHED)
    }
}




