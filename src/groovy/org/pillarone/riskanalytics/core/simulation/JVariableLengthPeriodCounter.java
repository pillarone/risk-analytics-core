package org.pillarone.riskanalytics.core.simulation;

import com.google.common.collect.Maps;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.util.List;
import java.util.TreeMap;

/**
 * A period counter which is backed by a list of dates. The last date defines the day following the end of the last period.
 */
public class JVariableLengthPeriodCounter extends ValuationDatePeriodCounter {

    Boolean annualPeriodsOnly;
    final TreeMap<DateTime, Integer> datePeriodMap = Maps.newTreeMap();


    public JVariableLengthPeriodCounter(List<DateTime> dates) {
        super(dates);
//        Note the constructor forces the dates to be sorted.
        for (int i = 0; i < this.dates.size(); i++) {
            datePeriodMap.put(dates.get(i), i);
        }
    }

    public int periodCount() {
        return dates.size() - 1;
    }

    public int belongsToPeriod(DateTime date) throws AfterSimulationEndException {
        checkDateBelongsInScope(date);
        return datePeriodMap.floorEntry(date).getValue();
    }

    private void checkDateBelongsInScope(DateTime date) {
        if (date.isAfter(datePeriodMap.lastKey()) || date.equals(datePeriodMap.lastKey()))
            throw new AfterSimulationEndException("");
        if (date.isBefore(datePeriodMap.firstKey())) throw new BeforeSimulationStartException("");
    }

    /**
     * @param date
     * @return valuation date at or before date
     */
    @Override
    public DateTime startOfPeriod(DateTime date) {
        checkDateBelongsInScope(date);
        return datePeriodMap.floorKey(date);
    }

    /**
     * @return the last valuation date
     */
    @Override
    public DateTime endOfLastPeriod() {
        return datePeriodMap.lastKey();
    }

    @Override
    public Boolean annualPeriodsOnly(boolean checkLastPeriodToo) {
        if (annualPeriodsOnly == null) {
            annualPeriodsOnly = true;
            int periods = checkLastPeriodToo ? periodCount() : periodCount() - 1;

            for (int i = 0; i < periods; i++) {
                Period period = new Period(dates.get(i), dates.get(i + 1));
                annualPeriodsOnly &= period.getYears() == 1 && period.getDays() == 0 && period.getMonths() == 0 && period.getHours() == 0 && period.getMinutes() == 0;
                if (!annualPeriodsOnly) {
                    break;
                }
            }
        }
        return annualPeriodsOnly;
    }
}
