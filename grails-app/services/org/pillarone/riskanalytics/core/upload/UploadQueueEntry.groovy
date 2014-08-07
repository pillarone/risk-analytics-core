package org.pillarone.riskanalytics.core.upload

import org.pillarone.riskanalytics.core.queue.AbstractQueueEntry

class UploadQueueEntry extends AbstractQueueEntry<UploadResult> {

    UploadQueueEntry(UploadConfiguration configuration, int priority) {
        super(configuration, priority)
    }

    UploadQueueEntry(UUID id) {
        super(id)
    }
}
