package org.pillarone.riskanalytics.core.upload

import org.pillarone.riskanalytics.core.queue.AbstractQueueEntry

class UploadQueueEntry extends AbstractQueueEntry<UploadQueueTaskContext> {

    UploadQueueEntry(UploadConfiguration configuration, int priority) {
        super(new UploadQueueTaskContext(configuration), priority)
    }

    UploadQueueEntry(UUID id) {
        super(id)
    }
}
