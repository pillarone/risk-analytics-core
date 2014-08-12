package org.pillarone.riskanalytics.core.upload

import org.pillarone.riskanalytics.core.queue.BasicQueueEntry

class UploadQueueEntry extends BasicQueueEntry<UploadConfiguration> {

    UploadQueueEntry(UploadConfiguration configuration, int priority) {
        super(new UploadQueueTaskContext(configuration), priority)
    }

    UploadQueueEntry(UUID id) {
        super(id)
    }

    @Override
    UploadQueueTaskContext getContext() {
        return super.getContext() as UploadQueueTaskContext
    }
}
