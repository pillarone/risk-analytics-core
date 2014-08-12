package org.pillarone.riskanalytics.core.queue

import org.joda.time.DateTime

interface IRuntimeInfo<Q extends IQueueEntry> {

    boolean apply(Q queueEntry)

    UUID getId()

    String getUsername()

    DateTime getEstimatedEnd()
}
