package org.pillarone.riskanalytics.core.simulation

import org.joda.time.DateTime
import org.joda.time.Period

class ContinuousPeriodCounterTests extends GroovyTestCase {

    DateTime date20091231 = new DateTime(2019, 12, 31, 0, 0, 0, 0)
    DateTime date20100101 = new DateTime(2010, 1, 1, 0, 0, 0, 0)
    DateTime date20100331 = new DateTime(2010, 3, 31, 0, 0, 0, 0)
    DateTime date20101231 = new DateTime(2010, 12, 31, 0, 0, 0, 0)
    DateTime date20110101 = new DateTime(2011, 1, 1, 0, 0, 0, 0)
    DateTime date20111231 = new DateTime(2011, 12, 31, 0, 0, 0, 0)
    DateTime date20120101 = new DateTime(2012, 1, 1, 0, 0, 0, 0)
    DateTime date20121231 = new DateTime(2012, 12, 31, 0, 0, 0, 0)
    DateTime date20130101 = new DateTime(2013, 1, 1, 0, 0, 0, 0)

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
        DateTime endOfPeriod = new DateTime(2010, 1, 1, 0, 0, 0, 0)
        DateTime endOfPeriodAfterIncrement = new DateTime(2011, 1, 1, 0, 0, 0, 0)
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
        DateTime endOfPeriod = new DateTime(2009, 7, 1, 0, 0, 0, 0)
        DateTime startOfPeriodAfterIncrement = new DateTime(2009, 7, 1, 0, 0, 0, 0)
        DateTime endOfPeriodAfterIncrement = new DateTime(2010, 1, 1, 0, 0, 0, 0)
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
        DateTime endOfPeriod = new DateTime(2009, 4, 1, 0, 0, 0, 0)
        DateTime startOfPeriodAfterIncrement = new DateTime(2009, 4, 1, 0, 0, 0, 0)
        DateTime endOfPeriodAfterIncrement = new DateTime(2009, 7, 1, 0, 0, 0, 0)
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
        DateTime endOfPeriod = new DateTime(2009, 2, 1, 0, 0, 0, 0)
        DateTime startOfPeriodAfterIncrement = new DateTime(2009, 2, 1, 0, 0, 0, 0)
        DateTime endOfPeriodAfterIncrement = new DateTime(2009, 3, 1, 0, 0, 0, 0)
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
        DateTime endOfPeriod = new DateTime(2008, 2, 1, 0, 0, 0, 0)
        DateTime startOfPeriodAfterIncrement = new DateTime(2008, 2, 1, 0, 0, 0, 0)
        DateTime endOfPeriodAfterIncrement = new DateTime(2008, 3, 1, 0, 0, 0, 0)
        ContinuousPeriodCounter counter = new ContinuousPeriodCounter(start, PeriodBase.MONTHLY.toPeriod())

        assertEquals "initial period start", startOfPeriod, counter.getCurrentPeriodStart()
        assertEquals "initial period end", endOfPeriod, counter.currentPeriodEnd

        counter++

        assertEquals "period start after increment", startOfPeriodAfterIncrement, counter.getCurrentPeriodStart()
        assertEquals "period end after increment", endOfPeriodAfterIncrement, counter.currentPeriodEnd
    }

    void testBelongsToPeriod() {
        ContinuousPeriodCounter counter = new ContinuousPeriodCounter(date20100101, PeriodBase.YEARLY.toPeriod())

        assertEquals "period 0 for 2010-01-01", 0, counter.belongsToPeriod(date20100101)
        assertEquals "period 0 for 2010-03-31", 0, counter.belongsToPeriod(date20100331)
        assertEquals "period 0 for 2010-12-31", 0, counter.belongsToPeriod(date20101231)
        assertEquals "period 1 for 2011-01-01", 1, counter.belongsToPeriod(date20110101)
        assertEquals "period 1 for 2011-12-31", 1, counter.belongsToPeriod(date20111231)
        assertEquals "period 2 for 2012-01-01", 2, counter.belongsToPeriod(date20120101)
        assertEquals "period 2 for 2012-12-31", 2, counter.belongsToPeriod(date20121231)

        shouldFail(BeforeSimulationStartException, { counter.belongsToPeriod(new DateTime(2009, 12, 31, 0,0,0,0))})
    }

    void testBelongsToCurrentPeriod() {
        ContinuousPeriodCounter counter = new ContinuousPeriodCounter(date20100101, PeriodBase.YEARLY.toPeriod())

        assertTrue  "2010-01-01 in 2010", counter.belongsToCurrentPeriod(date20100101)
        assertTrue  "2010-03-31 in 2010", counter.belongsToCurrentPeriod(date20100331)
        assertTrue  "2010-12-31 in 2010", counter.belongsToCurrentPeriod(date20101231)
        assertFalse "2011-01-01 in 2010", counter.belongsToCurrentPeriod(date20110101)
        assertFalse "2011-12-31 in 2010", counter.belongsToCurrentPeriod(date20111231)
        assertFalse "2012-01-01 in 2010", counter.belongsToCurrentPeriod(date20120101)
        assertFalse "2012-12-31 in 2010", counter.belongsToCurrentPeriod(date20121231)

        counter.next()

        assertFalse "2010-01-01 in 2011", counter.belongsToCurrentPeriod(date20100101)
        assertFalse "2010-03-31 in 2011", counter.belongsToCurrentPeriod(date20100331)
        assertFalse "2010-12-31 in 2011", counter.belongsToCurrentPeriod(date20101231)
        assertTrue  "2011-01-01 in 2011", counter.belongsToCurrentPeriod(date20110101)
        assertTrue  "2011-12-31 in 2011", counter.belongsToCurrentPeriod(date20111231)
        assertFalse "2012-01-01 in 2011", counter.belongsToCurrentPeriod(date20120101)
        assertFalse "2012-12-31 in 2011", counter.belongsToCurrentPeriod(date20121231)

    }

    void testStartOfPeriod() {
        ContinuousPeriodCounter counter = new ContinuousPeriodCounter(date20100101, PeriodBase.YEARLY.toPeriod())

        assertEquals "period 0 for 2010-01-01", date20100101, counter.startOfPeriod(date20100101)
        assertEquals "period 0 for 2010-03-31", date20100101, counter.startOfPeriod(date20100331)
        assertEquals "period 0 for 2010-12-31", date20100101, counter.startOfPeriod(date20101231)
        assertEquals "period 1 for 2011-01-01", date20110101, counter.startOfPeriod(date20110101)
        assertEquals "period 1 for 2011-12-31", date20110101, counter.startOfPeriod(date20111231)
        assertEquals "period 2 for 2012-01-01", date20120101, counter.startOfPeriod(date20120101)
        assertEquals "period 2 for 2012-12-31", date20120101, counter.startOfPeriod(date20121231)

        assertEquals "period 0 for 2010-01-01", date20100101, counter.startOfPeriod(0)
        assertEquals "period 1 for 2011-01-01", date20110101, counter.startOfPeriod(1)
        assertEquals "period 2 for 2012-01-01", date20120101, counter.startOfPeriod(2)
    }

    void testEndOfPeriod() {
        ContinuousPeriodCounter counter = new ContinuousPeriodCounter(date20100101, PeriodBase.YEARLY.toPeriod())

        assertEquals "period 0 for 2010-01-01", date20110101, counter.endOfPeriod(date20100101)
        assertEquals "period 0 for 2010-03-31", date20110101, counter.endOfPeriod(date20100331)
        assertEquals "period 0 for 2010-12-31", date20110101, counter.endOfPeriod(date20101231)
        assertEquals "period 1 for 2011-01-01", date20120101, counter.endOfPeriod(date20110101)
        assertEquals "period 1 for 2011-12-31", date20120101, counter.endOfPeriod(date20111231)
        assertEquals "period 2 for 2012-01-01", date20130101, counter.endOfPeriod(date20120101)
        assertEquals "period 2 for 2012-12-31", date20130101, counter.endOfPeriod(date20121231)

        assertEquals "end of period 0 for 2011-01-01", date20110101, counter.endOfPeriod(0)
        assertEquals "end of period 0 for 2012-01-01", date20120101, counter.endOfPeriod(1)
        assertEquals "end of period 0 for 2013-01-01", date20130101, counter.endOfPeriod(2)
    }

    void testStartOfFirstPeriod() {
        ContinuousPeriodCounter counter = new ContinuousPeriodCounter(date20100101, PeriodBase.YEARLY.toPeriod())

        assertEquals "start of projection horizon", date20100101, counter.startOfFirstPeriod()
    }

    void testAnnualPeriodsOnly() {
        ContinuousPeriodCounter counter = new ContinuousPeriodCounter(date20110101, PeriodBase.YEARLY.toPeriod())
        assertTrue "annual period", counter.annualPeriodsOnly(false)

        counter = new ContinuousPeriodCounter(date20110101, new Period(1, 1, 0, 0, 0, 0, 0, 0))
        assertFalse "annual period", counter.annualPeriodsOnly(false)
    }
}