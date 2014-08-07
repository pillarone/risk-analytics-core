package org.pillarone.riskanalytics.core.upload
import org.pillarone.riskanalytics.core.queue.IRuntimeInfo

class UploadRuntimeInfo implements Comparable<UploadRuntimeInfo>, IRuntimeInfo<UploadQueueEntry> {

    @Override
    int compareTo(UploadRuntimeInfo o) {
        return 0
    }

    @Override
    boolean apply(UploadQueueEntry queueEntry) {
        return false
    }

    @Override
    UUID getId() {
        return null
    }
}
