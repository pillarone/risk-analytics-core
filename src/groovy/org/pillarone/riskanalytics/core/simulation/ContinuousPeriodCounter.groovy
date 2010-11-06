package org.pillarone.riskanalytics.core.simulation

import org.joda.time.DateTime
import org.joda.time.Period
import org.apache.commons.lang.NotImplementedException

/**
 * An implementation of {@code IPeriodCounter } in which every period has the same length.
 */
public class ContinuousPeriodCounter implements IPeriodCounter {

    private DateTime startOfFirstPeriod

    int periodCount = 0
    private Period periodLength


    public ContinuousPeriodCounter(DateTime startDate, Period period) {
        startOfFirstPeriod = startDate
        this.periodLength = period
    }

    ContinuousPeriodCounter next() {
        periodCount++
        return this
    }

    public void reset() {
        periodCount = 0
    }

    DateTime getCurrentPeriodStart() {
        return getPeriodStart(periodCount)
    }

    protected DateTime getPeriodStart(int periodIndex) {
        periodStartToDate(periodIndex).dayOfMonth().withMinimumValue()
    }

    DateTime getCurrentPeriodEnd() {
        return getPeriodEnd(periodCount)
    }

    protected DateTime getPeriodEnd(int periodIndex) {
        DateTime startOfNext = new DateTime(getPeriodStart(periodIndex + 1))
        return startOfNext.minusDays(1)
    }

    DateTime getNextPeriodStart() {
        return getPeriodStart(periodCount + 1)
    }

    private DateTime periodStartToDate(int periodIndex) {
        return startOfFirstPeriod.withPeriodAdded(periodLength, periodIndex)
    }

    public boolean periodIncludesBeginningOfYear() {
        int startDayOfYear = getCurrentPeriodStart().dayOfYear().get()
        int endDayOfYear = getNextPeriodStart().dayOfYear().get()
        if (startDayOfYear == 1) {
            return true
        }
        else if (endDayOfYear == 1) {
            return false
        }
        else if (startDayOfYear > endDayOfYear) {
                return true
            }
        return false
    }

    def int belongsToPeriod(DateTime date) {
        throw new NotImplementedException();
    }
}
