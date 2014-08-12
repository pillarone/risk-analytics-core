package org.pillarone.riskanalytics.core.queue
import org.joda.time.DateTime

interface IQueueTaskContext<K> {

    K getConfiguration()

    DateTime getEstimatedEnd()

    int getProgress()

    String getUsername()
}
