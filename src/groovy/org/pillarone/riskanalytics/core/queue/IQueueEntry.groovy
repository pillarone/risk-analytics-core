package org.pillarone.riskanalytics.core.queue

interface IQueueEntry<K> extends Comparable<IQueueEntry<K>> {

    UUID getId()

    int getPriority()

    long getOfferedNanoTime()

    IQueueTaskContext<K> getContext()
}
