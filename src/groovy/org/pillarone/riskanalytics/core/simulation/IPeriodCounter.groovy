package org.pillarone.riskanalytics.core.simulation

import org.joda.time.DateTime

public interface IPeriodCounter {

    /**
     * Resets the Period counter to its initial state.
     * After a call to reset a period counter should behave like a newly create one.
     */
    void reset()

    /**
     * Advances the counter to the next period.
     * In groovy this also overrides the ++ operator.
     */
    IPeriodCounter next()

    /**
     * Returns the start date of the current period
     */
    DateTime getCurrentPeriodStart()

    /**
     * Returns the end date of the current period
     */
    DateTime getCurrentPeriodEnd()

    /**
     * Returns the start date of the next period
     */
    DateTime getNextPeriodStart()

    /**
     * Return true if the current period includes January 1
     */
    boolean periodIncludesBeginningOfYear()

    /**
     * @param date
     * @return period number containing the date
     */
    int belongsToPeriod(DateTime date)

    DateTime getPeriodStart(int periodIndex)

    DateTime getPeriodEnd(int periodIndex)

}