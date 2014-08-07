package org.pillarone.riskanalytics.core.queue

interface IQueueEntry<R extends IResult> extends Comparable<IQueueEntry> {

    UUID getId()

    int getPriority()

    long getOfferedNanoTime()

    void setResult(R result)

    R getResult()
}
