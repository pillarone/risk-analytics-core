package org.pillarone.riskanalytics.core.queue

interface IRuntimeInfo<Q extends IQueueEntry> {

    boolean apply(Q queueEntry)

    UUID getId()
}
