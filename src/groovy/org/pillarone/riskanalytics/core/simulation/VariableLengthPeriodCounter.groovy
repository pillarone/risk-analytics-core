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

    int belongsToPeriod(DateTime date) {
        return super.belongsToPeriod(date)
    }
}
