package org.pillarone.riskanalytics.core.simulation

import org.joda.time.DateTime

/**
 * A period counter which is backed by a list of dates. Naming of this class may be misleading as this implementation
 * has no underlying period concept but is based on valuation dates. Models based on this implementation will be run
 * at certain valuation dates and evaluating for these dates cashflows, calculating interest, ...
 * Implementation of methods may therefore be different for than in period base implementations!
 */
class ValuationDatePeriodCounter implements ILimitedPeriodCounter {

    protected List<DateTime> dates
    int currentPeriod

    public ValuationDatePeriodCounter(List<DateTime> dates) {
        currentPeriod = 0
        this.dates = Collections.unmodifiableList( dates.sort() )
    }

    void reset() {
        currentPeriod = 0
    }

    IPeriodCounter next() {
        currentPeriod++
        return this
    }

    /**
     * @return current valuation date
     */
    DateTime getCurrentPeriodStart() {
        dates.get(currentPeriod)
    }

    /**
     * @return next valuation date
     */
    DateTime getCurrentPeriodEnd() {
        getNextPeriodStart()
    }

    /**
     * @return next valuation date
     */
    DateTime getNextPeriodStart() {
        if (currentPeriod + 1 < dates.size()) {
            return dates.get(currentPeriod + 1)
        }
        else {
            throw new UnsupportedOperationException("Last period of a valuation date period counter does not have an end date or next period date")
        }
    }

    /**
     * @return true if the current valuation and next valuation date belong to different years
     */
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
            throw new UnsupportedOperationException("Unable to determine for last valuation date")
        }
    }

    /**
     * @return number of valuation dates
     */
    int periodCount() {
        return dates.size()
    }

    /**
     * @param date
     * @return date is after ith valuation date
     */
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

    /**
     * @param date
     * @return true if date belongs to current period, false if not and if the date is throwing a
     *          NotInProjectionHorizon exception
     */
    boolean belongsToCurrentPeriod(DateTime date) {
        try {
            int period = belongsToPeriod(date)
            return period == currentPeriod
        }
        catch (NotInProjectionHorizon ex) {
            return false
        }
    }

    /**
     * @param date
     * @return valuation date at or before date
     */
    DateTime startOfPeriod(DateTime date) {
        return dates[belongsToPeriod(date)]
    }

    /**
     * @param period
     * @return ith valuation date
     * @throws AfterSimulationEndException if period is greater than number of valuation dates
     */
    DateTime startOfPeriod(int period) throws AfterSimulationEndException {
        if (period >= dates.size()) throw new AfterSimulationEndException()
        return dates[period]
    }

    /**
     * @param date
     * @return valuation date after date
     * @throws AfterSimulationEndException if there is no valuation date after date
     */
    DateTime endOfPeriod(DateTime date) throws AfterSimulationEndException {
        int period = belongsToPeriod(date) + 1
        if (period >= dates.size()) throw new AfterSimulationEndException()
        return dates[period]
    }

    /**
     * @param period
     * @return ith + 1 valudation date
     * @throws AfterSimulationEndException if period is equal or greater than number of valuation dates
     */
    DateTime endOfPeriod(int period) throws AfterSimulationEndException {
        if (period >= dates.size() - 1) throw new AfterSimulationEndException()
        return dates[period + 1]
    }

    /**
     * @return false as this period counter is not used in a period ctx
     */
    @Override
    boolean annualPeriodsOnly() {
        return false
    }
/**
     * @return first valuation date
     */
    DateTime startOfFirstPeriod() {
        return dates[0]
    }

    /**
     * @return the last valuation date
     */
    DateTime endOfLastPeriod() {
        return dates[-1]
    }

    List<DateTime> periodDates() {
        return dates
    }
}
