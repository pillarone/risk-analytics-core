package org.pillarone.riskanalytics.core.simulation

import org.joda.time.DateTime
import org.apache.commons.lang.NotImplementedException

/**
 * A period counter which is backed by a list of dates.
 * Because the period length may vary, certain operations are not supported in the last period.
 */
class ValuationDatePeriodCounter implements ILimitedPeriodCounter {

    protected List<DateTime> dates
    int currentPeriod

    public ValuationDatePeriodCounter(List<DateTime> dates) {
        currentPeriod = 0
        this.dates = dates.sort()
    }

    void reset() {
        currentPeriod = 0
    }

    IPeriodCounter next() {
        currentPeriod++
        return this
    }

    DateTime getCurrentPeriodStart() {
        dates.get(currentPeriod)
    }

    DateTime getCurrentPeriodEnd() {
        getNextPeriodStart()
    }

    DateTime getNextPeriodStart() {
        if (currentPeriod + 1 < dates.size()) {
            return dates.get(currentPeriod + 1)
        } else {
            throw new UnsupportedOperationException("Last period of a variable length period counter does not have an end date or next period date")
        }
    }

    boolean periodIncludesBeginningOfYear() {
        if (currentPeriod + 1 < dates.size()) {
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
        } else {
            throw new UnsupportedOperationException("Unable to determine for last period")
        }
    }

    int periodCount() {
        return dates.size()
    }

    int belongsToPeriod(DateTime date) {
        if (date.isBefore(startOfFirstPeriod())) throw new BeforeSimulationStartException()
        if (date.isAfter(endOfLastPeriod())) throw new AfterSimulationEndException()
        int period = -1
        for (DateTime periodStart : dates) {
            if (periodStart.isAfter(date)) {
                return period
            }
            else {
                period++
            }
        }
        return period
    }

    DateTime startOfPeriod(DateTime date) {
        return dates[belongsToPeriod(date)]
    }

    DateTime startOfPeriod(int period) {
        return dates[period]
    }

    DateTime endOfPeriod(DateTime date) {
        return dates[belongsToPeriod(date) + 1]
    }

    DateTime endOfPeriod(int period) {
        return dates[period + 1]
    }

    DateTime startOfFirstPeriod() {
        return dates[0]
    }

    DateTime endOfLastPeriod() {
        return dates[-1]
    }
}
