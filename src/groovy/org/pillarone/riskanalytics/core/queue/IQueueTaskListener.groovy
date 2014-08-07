package org.pillarone.riskanalytics.core.queue

interface IQueueTaskListener {
    void apply(IQueueTaskFuture future)
}