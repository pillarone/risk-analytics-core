package org.pillarone.riskanalytics.core.simulation

import org.joda.time.DateTime


class VariableLengthPeriodCounterTests extends GroovyTestCase {

    DateTime date20080101 = new DateTime(2008, 1, 1, 0, 0, 0, 0)
    DateTime date20090101 = new DateTime(2009, 1, 1, 0, 0, 0, 0)
    DateTime date20090325 = new DateTime(2009, 3, 25, 0, 0, 0, 0)
    DateTime date20090909 = new DateTime(2009, 9, 9, 0, 0, 0, 0)
    DateTime date20110102 = new DateTime(2011, 1, 2, 0, 0, 0, 0)
    DateTime date20091231 = new DateTime(2019, 12, 31, 0, 0, 0, 0)
    DateTime date20100101 = new DateTime(2010, 1, 1, 0, 0, 0, 0)
    DateTime date20100331 = new DateTime(2010, 3, 31, 0, 0, 0, 0)
    DateTime date20101231 = new DateTime(2010, 12, 31, 0, 0, 0, 0)
    DateTime date20110101 = new DateTime(2011, 1, 1, 0, 0, 0, 0)
    DateTime date20111231 = new DateTime(2011, 12, 31, 0, 0, 0, 0)
    DateTime date20120101 = new DateTime(2012, 1, 1, 0, 0, 0, 0)
    DateTime date20121231 = new DateTime(2012, 12, 31, 0, 0, 0, 0)
    DateTime date20130101 = new DateTime(2013, 1, 1, 0, 0, 0, 0)

    VariableLengthPeriodCounter periodCounter
    JVariableLengthPeriodCounter jperiodCounter
    List<DateTime> dates

    void setUp() {
        dates = [
                date20090101,
                date20090325,
                date20090909,
                date20110102
        ]
        periodCounter = new VariableLengthPeriodCounter(dates)
        jperiodCounter = new JVariableLengthPeriodCounter(dates)
    }

    void testNext() {
        assertEquals 0, periodCounter.currentPeriod
        assertEquals 0, jperiodCounter.currentPeriod

        periodCounter.next()
        jperiodCounter.next()
        assertEquals 1, periodCounter.currentPeriod
        assertEquals 1, jperiodCounter.currentPeriod

        periodCounter++
        jperiodCounter.next()
        assertEquals 2, periodCounter.currentPeriod
        assertEquals 2, jperiodCounter.currentPeriod
    }

    void testReset() {
        assertEquals 0, periodCounter.currentPeriod
        assertEquals 0, jperiodCounter.currentPeriod

        periodCounter.next()
        jperiodCounter.next()
        assertEquals 1, periodCounter.currentPeriod
        assertEquals 1,jperiodCounter.currentPeriod

        periodCounter.reset()
        jperiodCounter.reset()
        assertEquals 0, periodCounter.currentPeriod
        assertEquals 0, jperiodCounter.currentPeriod
    }

    void testCurrentStartDate() {
        assertEquals dates[0].millis, periodCounter.currentPeriodStart.millis
        assertEquals dates[0].millis, jperiodCounter.currentPeriodStart.millis
        periodCounter++
        jperiodCounter++
        assertEquals dates[1].millis, periodCounter.currentPeriodStart.millis
        assertEquals dates[1].millis, jperiodCounter.currentPeriodStart.millis
        periodCounter++
        jperiodCounter++
        assertEquals dates[2].millis, periodCounter.currentPeriodStart.millis
        assertEquals dates[2].millis, jperiodCounter.currentPeriodStart.millis
        periodCounter++
        jperiodCounter++
        assertEquals dates[3].millis, periodCounter.currentPeriodStart.millis
        assertEquals dates[3].millis, jperiodCounter.currentPeriodStart.millis
    }

    void testNextStartDate() {
        assertEquals dates[1].millis, periodCounter.nextPeriodStart.millis
        periodCounter++
        assertEquals dates[2].millis, periodCounter.nextPeriodStart.millis
        periodCounter++
        assertEquals dates[3].millis, periodCounter.nextPeriodStart.millis
        periodCounter++
        shouldFail(UnsupportedOperationException) {
            periodCounter.nextPeriodStart
        }
    }

    void testCurrentEndDate() {
        assertEquals dates[1].millis, periodCounter.currentPeriodEnd.millis
        periodCounter++
        assertEquals dates[2].millis, periodCounter.currentPeriodEnd.millis
        periodCounter++
        assertEquals dates[3].millis, periodCounter.currentPeriodEnd.millis
        periodCounter++
        shouldFail(UnsupportedOperationException) {
            periodCounter.currentPeriodEnd.millis
        }
    }

    void testContainsBeginning() {
        assertTrue periodCounter.periodIncludesBeginningOfYear()
        periodCounter++
        assertFalse periodCounter.periodIncludesBeginningOfYear()
        periodCounter++
        assertTrue periodCounter.periodIncludesBeginningOfYear()
        periodCounter++
        shouldFail(UnsupportedOperationException) {
            periodCounter.periodIncludesBeginningOfYear()
        }
    }

    void testBelongsToPeriod() {
        assertEquals "period 0 for 2009-01-01", 0, periodCounter.belongsToPeriod(date20090101)
        assertEquals "period 1 for 2009-03-25", 1, periodCounter.belongsToPeriod(date20090325)
        assertEquals "period 1 for 2009-09-09", 2, periodCounter.belongsToPeriod(date20090909)
        assertEquals "period 2 for 2010-01-01", 2, periodCounter.belongsToPeriod(date20100101)
        assertEquals "period 2 for 2010-03-31", 2, periodCounter.belongsToPeriod(date20100331)
        assertEquals "period 2 for 2010-12-31", 2, periodCounter.belongsToPeriod(date20101231)
        assertEquals "period 2 for 2011-01-01", 2, periodCounter.belongsToPeriod(date20110101)

        shouldFail(BeforeSimulationStartException, { periodCounter.belongsToPeriod(date20080101)})
        shouldFail(AfterSimulationEndException, { periodCounter.belongsToPeriod(date20111231)})
        shouldFail(AfterSimulationEndException, { periodCounter.belongsToPeriod(date20110102)})

        assertEquals "period 0 for 2009-01-01", 0, jperiodCounter.belongsToPeriod(date20090101)
        assertEquals "period 1 for 2009-03-25", 1, jperiodCounter.belongsToPeriod(date20090325)
        assertEquals "period 1 for 2009-09-09", 2, jperiodCounter.belongsToPeriod(date20090909)
        assertEquals "period 2 for 2010-01-01", 2, jperiodCounter.belongsToPeriod(date20100101)
        assertEquals "period 2 for 2010-03-31", 2, jperiodCounter.belongsToPeriod(date20100331)
        assertEquals "period 2 for 2010-12-31", 2, jperiodCounter.belongsToPeriod(date20101231)
        assertEquals "period 2 for 2011-01-01", 2, jperiodCounter.belongsToPeriod(date20110101)

        shouldFail(BeforeSimulationStartException, { jperiodCounter.belongsToPeriod(date20080101)})
        shouldFail(AfterSimulationEndException, { jperiodCounter.belongsToPeriod(date20111231)})
        shouldFail(AfterSimulationEndException, { jperiodCounter.belongsToPeriod(date20110102)})
    }

    void testStartOfPeriod() {
        assertEquals "period 2 for 2010-01-01", date20090909, periodCounter.startOfPeriod(date20100101)
        assertEquals "period 2 for 2010-03-31", date20090909, periodCounter.startOfPeriod(date20100331)
        assertEquals "period 2 for 2010-12-31", date20090909, periodCounter.startOfPeriod(date20101231)
        assertEquals "period 2 for 2011-01-01", date20090909, periodCounter.startOfPeriod(date20110101)

        assertEquals "start of period 0", date20090101, periodCounter.startOfPeriod(0)
        assertEquals "start of period 1", date20090325, periodCounter.startOfPeriod(1)
        assertEquals "start of period 2", date20090909, periodCounter.startOfPeriod(2)

        assertEquals "period 2 for 2010-01-01", date20090909, jperiodCounter.startOfPeriod(date20100101)
        assertEquals "period 2 for 2010-03-31", date20090909, jperiodCounter.startOfPeriod(date20100331)
        assertEquals "period 2 for 2010-12-31", date20090909, jperiodCounter.startOfPeriod(date20101231)
        assertEquals "period 2 for 2011-01-01", date20090909, jperiodCounter.startOfPeriod(date20110101)

        assertEquals "start of period 0", date20090101, jperiodCounter.startOfPeriod(0)
        assertEquals "start of period 1", date20090325, jperiodCounter.startOfPeriod(1)
        assertEquals "start of period 2", date20090909, jperiodCounter.startOfPeriod(2)
    }

    void testEndOfPeriod() {
        assertEquals "period 0 for 2010-01-01", date20110102, periodCounter.endOfPeriod(date20100101)
        assertEquals "period 0 for 2010-03-31", date20110102, periodCounter.endOfPeriod(date20100331)
        assertEquals "period 0 for 2010-12-31", date20110102, periodCounter.endOfPeriod(date20101231)
        assertEquals "period 1 for 2011-01-01", date20110102, periodCounter.endOfPeriod(date20110101)

        assertEquals "end of period 0", date20090325, periodCounter.endOfPeriod(0)
        assertEquals "end of period 1", date20090909, periodCounter.endOfPeriod(1)
        assertEquals "end of period 2", date20110102, periodCounter.endOfPeriod(2)

        assertEquals "period 0 for 2010-01-01", date20110102, jperiodCounter.endOfPeriod(date20100101)
        assertEquals "period 0 for 2010-03-31", date20110102, jperiodCounter.endOfPeriod(date20100331)
        assertEquals "period 0 for 2010-12-31", date20110102, jperiodCounter.endOfPeriod(date20101231)
        assertEquals "period 1 for 2011-01-01", date20110102, jperiodCounter.endOfPeriod(date20110101)

        assertEquals "end of period 0", date20090325, jperiodCounter.endOfPeriod(0)
        assertEquals "end of period 1", date20090909, jperiodCounter.endOfPeriod(1)
        assertEquals "end of period 2", date20110102, jperiodCounter.endOfPeriod(2)
    }

    void testStartOfFirstPeriod() {
        assertEquals "start of projection horizon", date20090101, periodCounter.startOfFirstPeriod()
        assertEquals "start of projection horizon", date20090101, jperiodCounter.startOfFirstPeriod()
    }

    void testEndOfLastPeriod() {
        assertEquals "end of projection horizon", date20110102, periodCounter.endOfLastPeriod()
        assertEquals "end of projection horizon", date20110102, jperiodCounter.endOfLastPeriod()
    }

    void testAnnualPeriodsOnly() {
        assertFalse "unequal period lengths", periodCounter.annualPeriodsOnly(false)
        assertFalse "unequal period lengths", jperiodCounter.annualPeriodsOnly(false)

        VariableLengthPeriodCounter counter = new VariableLengthPeriodCounter([date20080101, date20090101, date20100101, date20130101])
        JVariableLengthPeriodCounter jcounter = new JVariableLengthPeriodCounter([date20080101, date20090101, date20100101, date20130101])
        assertFalse "unequal period lengths, last period", counter.annualPeriodsOnly(true)
        assertFalse "unequal period lengths, last period", jcounter.annualPeriodsOnly(true)

        counter = new VariableLengthPeriodCounter([date20080101, date20090101, date20100101, date20110101])
        jcounter = new JVariableLengthPeriodCounter([date20080101, date20090101, date20100101, date20110101])
        assertTrue "three annual periods", counter.annualPeriodsOnly(true)
        assertTrue "three annual periods", counter.annualPeriodsOnly(true)
    }
}
