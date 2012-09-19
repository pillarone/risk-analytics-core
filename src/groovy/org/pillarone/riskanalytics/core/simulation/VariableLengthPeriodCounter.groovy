package org.pillarone.riskanalytics.core.simulation

import org.joda.time.DateTime
import org.joda.time.DateTimeUtils

/**
 * A period counter which is backed by a list of dates. The last date defines the day following the end of the last period.
 */
class VariableLengthPeriodCounter extends ValuationDatePeriodCounter {

    public VariableLengthPeriodCounter(List<DateTime> dates) {
        super(dates)
    }

    int periodCount() {
        return dates.size() - 1
    }

    public int belongsToPeriod(DateTime date) throws AfterSimulationEndException {
        if (!date.isBefore(endOfLastPeriod())) throw new AfterSimulationEndException("Date : " + date.toString() + """
            is after the end of the simulation as defined by the period counter. End date """ + endOfLastPeriod().toString())
        return super.belongsToPeriod(date)
    }
}
