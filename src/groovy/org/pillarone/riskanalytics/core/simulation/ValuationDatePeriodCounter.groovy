package org.pillarone.riskanalytics.core.simulation

import org.joda.time.DateTime

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
        getNextPeriodStart().minusDays(1)
    }

    DateTime getNextPeriodStart() {
        if (currentPeriod + 1 < dates.size()) {
            return dates.get(currentPeriod + 1)
        }
        else {
            throw new UnsupportedOperationException("Last period of a variable length period counter does not have an end date or next period date")
        }
    }

    DateTime getPeriodStart(int periodIndex) {
        if (periodIndex < 0 || periodIndex >= dates.size()) {
            throw new UnsupportedOperationException("Period out of range, impossible to determine start date")
        }
        return dates.get(periodIndex)
    }

    DateTime getPeriodEnd(int periodIndex) {
        if (periodIndex >= dates.size() - 1) {
            throw new UnsupportedOperationException("Period out of range, impossible to determine end date")
        }
        return dates.get(periodIndex + 1).minusDays(1)
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
        }
        else {
            throw new UnsupportedOperationException("Unable to determine for last period")
        }
    }

    int periodCount() {
        return dates.size()
    }

    int belongsToPeriod(DateTime date) {
        int period = -1
        for (DateTime periodStart: dates) {
            if (periodStart.isAfter(date)) {
                return period
            }
            else {
                period++
            }
        }
        return period
    }
}
