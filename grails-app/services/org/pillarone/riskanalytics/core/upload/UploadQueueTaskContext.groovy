package org.pillarone.riskanalytics.core.upload

import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.queue.IQueueTaskContext

class UploadQueueTaskContext implements IQueueTaskContext<UploadConfiguration> {
    final UploadConfiguration configuration
    List<String> errors = []
    UploadState uploadState = UploadState.PENDING
    int progress = 0

    UploadQueueTaskContext(UploadConfiguration configuration) {
        this.configuration = configuration
    }

    @Override
    DateTime getEstimatedEnd() {
        null
    }

    @Override
    int getProgress() {
        progress
    }

    @Override
    String getUsername() {
        configuration.username
    }
}
