package org.pillarone.riskanalytics.core.upload

import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.queue.IQueueTaskContext

class UploadQueueTaskContext implements IQueueTaskContext<UploadConfiguration> {
    final UploadConfiguration configuration
    private List<String> errors = []
    private UploadState uploadState = UploadState.PENDING
    volatile int progress = 0
    private final Object lock = new Object()

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

    void setUploadState(UploadState state) {
        synchronized (lock) {
            uploadState = state
        }
    }

    UploadState getUploadState() {
        return uploadState
    }

    void addError(String error) {
        synchronized (lock) {
            errors.add(error)
        }
    }

    List<String> getErrors() {
        synchronized (lock) {
            return new ArrayList<String>(errors)
        }
    }
}
