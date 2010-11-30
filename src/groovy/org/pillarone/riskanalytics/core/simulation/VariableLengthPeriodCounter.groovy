package org.pillarone.riskanalytics.core.simulation

import org.joda.time.DateTime

/**
 * A period counter which is backed by a list of dates. The last date defines the end of the last period.
 */
class VariableLengthPeriodCounter extends ValuationDatePeriodCounter {

    public VariableLengthPeriodCounter(List<DateTime> dates) {
        super(dates)
    }

    int periodCount() {
        return dates.size() - 1
    }

    DateTime getPeriodStart(int periodIndex) {
        if (periodIndex < 0 || periodIndex >= dates.size() - 1) {
            throw new UnsupportedOperationException("Period out of range, impossible to determine start date")
        }
        return dates.get(periodIndex)
    }

    DateTime getPeriodEnd(int periodIndex) {
        if (periodIndex < 0 || periodIndex >= dates.size() - 1) {
            throw new UnsupportedOperationException("Period out of range, impossible to determine end date")
        }
        if (periodIndex < dates.size() - 2) {
            return dates.get(periodIndex + 1).minusDays(1)
        }
        else {
            return dates.get(periodIndex + 1)
        }
    }

    int belongsToPeriod(DateTime date) {
        if (date.isAfter(dates[-1])) return -1
        return super.belongsToPeriod(date)
    }

}
