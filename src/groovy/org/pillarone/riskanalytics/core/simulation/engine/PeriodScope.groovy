package org.pillarone.riskanalytics.core.simulation.engine

import groovy.transform.CompileStatic
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.parameterization.ParameterApplicator
import org.pillarone.riskanalytics.core.simulation.IPeriodCounter

/**
 * The PeriodScope provides information that is valid for a single period
 */

@CompileStatic
public class PeriodScope {

    private static Log LOG = LogFactory.getLog(PeriodScope)

    def parameter
    int currentPeriod = 0

    ParameterApplicator parameterApplicator

    IPeriodCounter periodCounter

    void prepareNextPeriod() {
        if (LOG.isDebugEnabled()) LOG.debug "peparing next period"
        currentPeriod++
        if (periodCounter) {
            periodCounter++
        }
    }

    public boolean isFirstPeriod() {
        currentPeriod == 0
    }

    public DateTime getCurrentPeriodStartDate() {
        return periodCounter?.currentPeriodStart
    }

    public DateTime getNextPeriodStartDate() {
        return periodCounter?.nextPeriodStart
    }

    public boolean periodIncludesBeginningOfYear() {
        return periodCounter?.periodIncludesBeginningOfYear()
    }

    public void reset() {
        currentPeriod = 0
        periodCounter?.reset()
    }

    public String toString() {
        "current period: $currentPeriod, current period start date: $currentPeriodStartDate"
    }
}