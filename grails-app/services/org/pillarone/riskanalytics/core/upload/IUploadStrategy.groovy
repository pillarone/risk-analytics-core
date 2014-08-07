package org.pillarone.riskanalytics.core.upload

import org.pillarone.riskanalytics.core.queue.IQueueTaskFuture

interface IUploadStrategy {

    IQueueTaskFuture upload(UploadConfiguration configuration, int priority)
}
