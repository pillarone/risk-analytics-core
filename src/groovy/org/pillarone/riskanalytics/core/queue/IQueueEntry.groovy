package org.pillarone.riskanalytics.core.queue

interface IQueueEntry<R extends IQueueTaskContext> extends Comparable<IQueueEntry> {

    UUID getId()

    int getPriority()

    long getOfferedNanoTime()

    R getContext()
}
