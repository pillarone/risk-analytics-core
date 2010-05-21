package org.pillarone.riskanalytics.core

import org.pillarone.riskanalytics.core.batch.BatchRunService

class BatchRun {
    String name
    String comment
    Date executionTime
    boolean executed = false
    BatchRunService batchRunService

    static transients = ['batchRunService']

    static constraints = {
        name(unique: true)
        comment nullable: true
        executionTime nullable: true
    }
}
