package org.pillarone.riskanalytics.core.simulation

import org.joda.time.DateTime
import org.joda.time.Period

class ContinuousPeriodCounterTests extends GroovyTestCase {

    void testIncrement() {
        ContinuousPeriodCounter counter = new ContinuousPeriodCounter(new DateTime(2009, 1, 1, 0, 0, 0, 0), Period.years(1))
        assertEquals "initial period count", 0, counter.periodCount

        counter.next()
        assertEquals "incremented period count", 1, counter.periodCount
        counter++
        assertEquals "incremented period count", 2, counter.periodCount
    }

    void testReset() {
        ContinuousPeriodCounter counter = new ContinuousPeriodCounter(new DateTime(2009, 1, 1, 0, 0, 0, 0), Period.years(1))
        assertEquals "initial period count", 0, counter.periodCount
        counter++
        counter++
        assertEquals "incremented period count", 2, counter.periodCount

        counter.reset()
        assertEquals "period count after reset", 0, counter.periodCount
    }


    void testYearlyPeriods() {
        DateTime start = new DateTime(2009, 1, 5, 0, 0, 0, 0)
        DateTime startOfPeriod = new DateTime(2009, 1, 1, 0, 0, 0, 0)
        DateTime startOfPeriodAfterIncrement = new DateTime(2010, 1, 1, 0, 0, 0, 0)
        DateTime endOfPeriod = new DateTime(2009, 12, 31, 0, 0, 0, 0)
        DateTime endOfPeriodAfterIncrement = new DateTime(2010, 12, 31, 0, 0, 0, 0)
        ContinuousPeriodCounter counter = new ContinuousPeriodCounter(start, PeriodBase.YEARLY.toPeriod())

        assertEquals "initial period start", startOfPeriod, counter.getCurrentPeriodStart()
        assertEquals "initial period end", endOfPeriod, counter.currentPeriodEnd

        counter++

        assertEquals "period start after increment", startOfPeriodAfterIncrement, counter.getCurrentPeriodStart()
        assertEquals "period end after increment", endOfPeriodAfterIncrement, counter.currentPeriodEnd


    }

    void testHalfYearlyPeriods() {
        DateTime start = new DateTime(2009, 1, 5, 0, 0, 0, 0)
        DateTime startOfPeriod = new DateTime(2009, 1, 1, 0, 0, 0, 0)
        DateTime endOfPeriod = new DateTime(2009, 6, 30, 0, 0, 0, 0)
        DateTime startOfPeriodAfterIncrement = new DateTime(2009, 7, 1, 0, 0, 0, 0)
        DateTime endOfPeriodAfterIncrement = new DateTime(2009, 12, 31, 0, 0, 0, 0)
        ContinuousPeriodCounter counter = new ContinuousPeriodCounter(start, PeriodBase.HALF_YEARLY.toPeriod())

        assertEquals "initial period start", startOfPeriod, counter.getCurrentPeriodStart()
        assertEquals "initial period end", endOfPeriod, counter.currentPeriodEnd

        counter++

        assertEquals "period start after increment", startOfPeriodAfterIncrement, counter.getCurrentPeriodStart()
        assertEquals "period end after increment", endOfPeriodAfterIncrement, counter.currentPeriodEnd
    }

    void testQuarterlyPeriods() {
        DateTime start = new DateTime(2009, 1, 5, 0, 0, 0, 0)
        DateTime startOfPeriod = new DateTime(2009, 1, 1, 0, 0, 0, 0)
        DateTime endOfPeriod = new DateTime(2009, 3, 31, 0, 0, 0, 0)
        DateTime startOfPeriodAfterIncrement = new DateTime(2009, 4, 1, 0, 0, 0, 0)
        DateTime endOfPeriodAfterIncrement = new DateTime(2009, 6, 30, 0, 0, 0, 0)
        ContinuousPeriodCounter counter = new ContinuousPeriodCounter(start, PeriodBase.QUARTERLY.toPeriod())

        assertEquals "initial period start", startOfPeriod, counter.getCurrentPeriodStart()
        assertEquals "initial period end", endOfPeriod, counter.currentPeriodEnd

        counter++

        assertEquals "period start after increment", startOfPeriodAfterIncrement, counter.getCurrentPeriodStart()
        assertEquals "period end after increment", endOfPeriodAfterIncrement, counter.currentPeriodEnd
    }

    void testMonthlyPeriods() {
        DateTime start = new DateTime(2009, 1, 5, 0, 0, 0, 0)
        DateTime startOfPeriod = new DateTime(2009, 1, 1, 0, 0, 0, 0)
        DateTime endOfPeriod = new DateTime(2009, 1, 31, 0, 0, 0, 0)
        DateTime startOfPeriodAfterIncrement = new DateTime(2009, 2, 1, 0, 0, 0, 0)
        DateTime endOfPeriodAfterIncrement = new DateTime(2009, 2, 28, 0, 0, 0, 0)
        ContinuousPeriodCounter counter = new ContinuousPeriodCounter(start, PeriodBase.MONTHLY.toPeriod())

        assertEquals "initial period start", startOfPeriod, counter.getCurrentPeriodStart()
        assertEquals "initial period end", endOfPeriod, counter.currentPeriodEnd

        counter++

        assertEquals "period start after increment", startOfPeriodAfterIncrement, counter.getCurrentPeriodStart()
        assertEquals "period end after increment", endOfPeriodAfterIncrement, counter.currentPeriodEnd
    }

    void testMonthlyPeriodsWithLeapYear() {
        DateTime start = new DateTime(2008, 1, 5, 0, 0, 0, 0)
        DateTime startOfPeriod = new DateTime(2008, 1, 1, 0, 0, 0, 0)
        DateTime endOfPeriod = new DateTime(2008, 1, 31, 0, 0, 0, 0)
        DateTime startOfPeriodAfterIncrement = new DateTime(2008, 2, 1, 0, 0, 0, 0)
        DateTime endOfPeriodAfterIncrement = new DateTime(2008, 2, 29, 0, 0, 0, 0)
        ContinuousPeriodCounter counter = new ContinuousPeriodCounter(start, PeriodBase.MONTHLY.toPeriod())

        assertEquals "initial period start", startOfPeriod, counter.getCurrentPeriodStart()
        assertEquals "initial period end", endOfPeriod, counter.currentPeriodEnd

        counter++

        assertEquals "period start after increment", startOfPeriodAfterIncrement, counter.getCurrentPeriodStart()
        assertEquals "period end after increment", endOfPeriodAfterIncrement, counter.currentPeriodEnd
    }

}