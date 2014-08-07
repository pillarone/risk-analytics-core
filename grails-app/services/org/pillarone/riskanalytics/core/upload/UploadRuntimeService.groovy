package org.pillarone.riskanalytics.core.upload

import org.pillarone.riskanalytics.core.queue.AbstractRuntimeService
import org.pillarone.riskanalytics.core.queue.IQueueService

class UploadRuntimeService extends AbstractRuntimeService<UploadQueueEntry, UploadRuntimeInfo> {

    UploadQueueService uploadQueueService

    @Override
    IQueueService<UploadQueueEntry> getQueueService() {
        uploadQueueService
    }

    @Override
    UploadRuntimeInfo createRuntimeInfo(UploadQueueEntry queueEntry) {
        return new UploadRuntimeInfo()
    }

    @Override
    void postConstruct() {}

    @Override
    void preDestroy() {}
}
