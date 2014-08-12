package org.pillarone.riskanalytics.core.queue

interface IQueueTaskFuture {
    void stopListenAsync(IQueueTaskListener taskListener)

    void listenAsync(IQueueTaskListener uploadTaskListener)

    void cancel()

}
