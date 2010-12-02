package org.pillarone.riskanalytics.core.simulation

import org.joda.time.DateTime

/**
 * Periods are defined similarly to the joda time library in the sense that we assume a left closed and right opened
 * interval. Therefore end period methods will return the start date of the next period, which is actually not part
 * ot the period.
 */
public interface IPeriodCounter {

    /**
     * Resets the period counter to its initial state.
     * After a call to reset a period counter should behave like a newly create one.
     */
    void reset()

    /**
     * Advances the counter to the next period.
     * In groovy this also overrides the ++ operator.
     */
    IPeriodCounter next()

    /**
     * @return start date of current period
     */
    DateTime getCurrentPeriodStart()

    /**
     * @return end date of current period. It has to correspond to the next period start date
     */
    DateTime getCurrentPeriodEnd()

    /**
     * @return start date of next period
     */
    DateTime getNextPeriodStart()

    /**
     * @return true if the current period includes January 1
     */
    boolean periodIncludesBeginningOfYear()

    /**
     * @param date
     * @return period number date belongs to
     * @throws BeforeSimulationStartException if the date is before startOfFirstPeriod()
     * @throws AfterSimulationEndException if the date is after endOfLastPeriod()
     */
    int belongsToPeriod(DateTime date) throws BeforeSimulationStartException, AfterSimulationEndException

    DateTime startOfFirstPeriod()
    DateTime endOfLastPeriod()

    /**
     * @param date
     * @return start of the period the date belongs to
     */
    DateTime startOfPeriod(DateTime date)

    /**
     * @param period
     * @return start date of the period
     */
    DateTime startOfPeriod(int period)

    /**
     * @param date
     * @return end of the period the date belongs to
     */
    DateTime endOfPeriod(DateTime date)

    /**
     * @param period
     * @return end date of the period
     */
    DateTime endOfPeriod(int period)

    DateTime getPeriodStart(int periodIndex)

    DateTime getPeriodEnd(int periodIndex)

}