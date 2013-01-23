package org.pillarone.riskanalytics.core.simulation

import org.joda.time.DateTime
import org.joda.time.Period

class LimitedContinuousPeriodCounterTests extends GroovyTestCase {

    void testIncrement() {
        LimitedContinuousPeriodCounter counter = new LimitedContinuousPeriodCounter(new DateTime(2009, 1, 1, 0, 0, 0, 0), Period.years(1), 3)
        assertEquals "initial period count", 0, counter.periodCount

        counter.next()
        assertEquals "incremented period count", 1, counter.periodCount
        counter++
        assertEquals "incremented period count", 2, counter.periodCount
    }

    void testReset() {
        LimitedContinuousPeriodCounter counter = new LimitedContinuousPeriodCounter(new DateTime(2009, 1, 1, 0, 0, 0, 0), Period.years(1), 3)
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
        DateTime endOfPeriod = new DateTime(2010, 1, 1, 0, 0, 0, 0)
        DateTime endOfPeriodAfterIncrement = new DateTime(2011, 1, 1, 0, 0, 0, 0)
        LimitedContinuousPeriodCounter counter = new LimitedContinuousPeriodCounter(start, PeriodBase.YEARLY.toPeriod(), 4)

        assertEquals "initial period start", startOfPeriod, counter.getCurrentPeriodStart()
        assertEquals "initial period end", endOfPeriod, counter.currentPeriodEnd

        counter++

        assertEquals "period start after increment", startOfPeriodAfterIncrement, counter.getCurrentPeriodStart()
        assertEquals "period end after increment", endOfPeriodAfterIncrement, counter.currentPeriodEnd


    }

    void testHalfYearlyPeriods() {
        DateTime start = new DateTime(2009, 1, 5, 0, 0, 0, 0)
        DateTime startOfPeriod = new DateTime(2009, 1, 1, 0, 0, 0, 0)
        DateTime endOfPeriod = new DateTime(2009, 7, 1, 0, 0, 0, 0)
        DateTime startOfPeriodAfterIncrement = new DateTime(2009, 7, 1, 0, 0, 0, 0)
        DateTime endOfPeriodAfterIncrement = new DateTime(2010, 1, 1, 0, 0, 0, 0)
        LimitedContinuousPeriodCounter counter = new LimitedContinuousPeriodCounter(start, PeriodBase.HALF_YEARLY.toPeriod(), 4)

        assertEquals "initial period start", startOfPeriod, counter.getCurrentPeriodStart()
        assertEquals "initial period end", endOfPeriod, counter.currentPeriodEnd

        counter++

        assertEquals "period start after increment", startOfPeriodAfterIncrement, counter.getCurrentPeriodStart()
        assertEquals "period end after increment", endOfPeriodAfterIncrement, counter.currentPeriodEnd
    }

    void testQuarterlyPeriods() {
        DateTime start = new DateTime(2009, 1, 5, 0, 0, 0, 0)
        DateTime startOfPeriod = new DateTime(2009, 1, 1, 0, 0, 0, 0)
        DateTime endOfPeriod = new DateTime(2009, 4, 1, 0, 0, 0, 0)
        DateTime startOfPeriodAfterIncrement = new DateTime(2009, 4, 1, 0, 0, 0, 0)
        DateTime endOfPeriodAfterIncrement = new DateTime(2009, 7, 1, 0, 0, 0, 0)
        LimitedContinuousPeriodCounter counter = new LimitedContinuousPeriodCounter(start, PeriodBase.QUARTERLY.toPeriod(), 4)

        assertEquals "initial period start", startOfPeriod, counter.getCurrentPeriodStart()
        assertEquals "initial period end", endOfPeriod, counter.currentPeriodEnd

        counter++

        assertEquals "period start after increment", startOfPeriodAfterIncrement, counter.getCurrentPeriodStart()
        assertEquals "period end after increment", endOfPeriodAfterIncrement, counter.currentPeriodEnd
    }

    void testMonthlyPeriods() {
        DateTime start = new DateTime(2009, 1, 5, 0, 0, 0, 0)
        DateTime startOfPeriod = new DateTime(2009, 1, 1, 0, 0, 0, 0)
        DateTime endOfPeriod = new DateTime(2009, 2, 1, 0, 0, 0, 0)
        DateTime startOfPeriodAfterIncrement = new DateTime(2009, 2, 1, 0, 0, 0, 0)
        DateTime endOfPeriodAfterIncrement = new DateTime(2009, 3, 1, 0, 0, 0, 0)
        LimitedContinuousPeriodCounter counter = new LimitedContinuousPeriodCounter(start, PeriodBase.MONTHLY.toPeriod(), 4)

        assertEquals "initial period start", startOfPeriod, counter.getCurrentPeriodStart()
        assertEquals "initial period end", endOfPeriod, counter.currentPeriodEnd

        counter++

        assertEquals "period start after increment", startOfPeriodAfterIncrement, counter.getCurrentPeriodStart()
        assertEquals "period end after increment", endOfPeriodAfterIncrement, counter.currentPeriodEnd
    }

    void testMonthlyPeriodsWithLeapYear() {
        DateTime start = new DateTime(2008, 1, 5, 0, 0, 0, 0)
        DateTime startOfPeriod = new DateTime(2008, 1, 1, 0, 0, 0, 0)
        DateTime endOfPeriod = new DateTime(2008, 2, 1, 0, 0, 0, 0)
        DateTime startOfPeriodAfterIncrement = new DateTime(2008, 2, 1, 0, 0, 0, 0)
        DateTime endOfPeriodAfterIncrement = new DateTime(2008, 3, 1, 0, 0, 0, 0)
        LimitedContinuousPeriodCounter counter = new LimitedContinuousPeriodCounter(start, PeriodBase.MONTHLY.toPeriod(), 4)

        assertEquals "initial period start", startOfPeriod, counter.getCurrentPeriodStart()
        assertEquals "initial period end", endOfPeriod, counter.currentPeriodEnd

        counter++

        assertEquals "period start after increment", startOfPeriodAfterIncrement, counter.getCurrentPeriodStart()
        assertEquals "period end after increment", endOfPeriodAfterIncrement, counter.currentPeriodEnd
    }

    void testEndOfLastPeriod() {
        DateTime start = new DateTime(2008, 1, 5, 0, 0, 0, 0)
        LimitedContinuousPeriodCounter counter = new LimitedContinuousPeriodCounter(start, PeriodBase.MONTHLY.toPeriod(), 4)

        assertEquals "last period ends at 2008-06-01", new DateTime(2008, 6, 1, 0,0,0,0), counter.endOfLastPeriod()
    }

    void testPeriodDates() {
        DateTime start = new DateTime(2008, 1, 5, 0, 0, 0, 0)
        LimitedContinuousPeriodCounter counter = new LimitedContinuousPeriodCounter(start, PeriodBase.MONTHLY.toPeriod(), 4)

        assertEquals "check size", 6, counter.periodDates().size()
        assertEquals "check first date", counter.getPeriodStart(0), counter.periodDates()[0]
        assertEquals "check final date", counter.endOfLastPeriod(), counter.periodDates()[-1]

    }
}