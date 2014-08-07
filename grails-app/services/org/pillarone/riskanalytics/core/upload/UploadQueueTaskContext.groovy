package org.pillarone.riskanalytics.core.upload

import org.pillarone.riskanalytics.core.queue.IQueueTaskContext

class UploadQueueTaskContext implements IQueueTaskContext<UploadConfiguration> {
    final UploadConfiguration configuration
    List<String> errors = []
    UploadState uploadState = UploadState.PENDING

    UploadQueueTaskContext(UploadConfiguration configuration) {
        this.configuration = configuration
    }
}
