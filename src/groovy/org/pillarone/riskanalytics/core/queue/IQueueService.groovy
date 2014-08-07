package org.pillarone.riskanalytics.core.queue

interface IQueueService<Q extends IQueueEntry> {

    void removeQueueListener(QueueListener<Q> queueListener)

    void addQueueListener(QueueListener<Q> queueListener)

}