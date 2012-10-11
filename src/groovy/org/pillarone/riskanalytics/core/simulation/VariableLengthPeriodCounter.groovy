package org.pillarone.riskanalytics.core.simulation

import org.joda.time.DateTime
import org.joda.time.DateTimeUtils
import org.joda.time.Period

/**
 * A period counter which is backed by a list of dates. The last date defines the day following the end of the last period.
 */
class VariableLengthPeriodCounter extends ValuationDatePeriodCounter {

    Boolean annualPeriodsOnly

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

    @Override
    boolean annualPeriodsOnly() {
        if (annualPeriodsOnly == null) {
            annualPeriodsOnly = true
            for (int i = 0; i < dates.size() - 1; i++) {
                Period period = new Period(dates.get(i), dates.get(i+1));
                annualPeriodsOnly &= period.years == 1 && period.days == 0 && period.months == 0 && period.hours == 0 && period.minutes == 0
                if (!annualPeriodsOnly) {
                    break
                }
            }
        }
        return annualPeriodsOnly
    }
}
