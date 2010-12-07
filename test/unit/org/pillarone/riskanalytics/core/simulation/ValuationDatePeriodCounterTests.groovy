package org.pillarone.riskanalytics.core.simulation

import org.joda.time.DateTime

class ValuationDatePeriodCounterTests extends GroovyTestCase {

    ValuationDatePeriodCounter periodCounter
    List<DateTime> dates

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

    void setUp() {
        dates = [
                date20090101,
                date20090325,
                date20090909,
                date20110102
        ]
        periodCounter = new ValuationDatePeriodCounter(dates)
    }

    void testNext() {
        assertEquals 0, periodCounter.currentPeriod

        periodCounter.next()
        assertEquals 1, periodCounter.currentPeriod

        periodCounter++
        assertEquals 2, periodCounter.currentPeriod
    }

    void testReset() {
        assertEquals 0, periodCounter.currentPeriod

        periodCounter.next()
        assertEquals 1, periodCounter.currentPeriod

        periodCounter.reset()
        assertEquals 0, periodCounter.currentPeriod
    }

    void testCurrentStartDate() {
        assertEquals dates[0].millis, periodCounter.currentPeriodStart.millis
        periodCounter++
        assertEquals dates[1].millis, periodCounter.currentPeriodStart.millis
        periodCounter++
        assertEquals dates[2].millis, periodCounter.currentPeriodStart.millis
        periodCounter++
        assertEquals dates[3].millis, periodCounter.currentPeriodStart.millis
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
        assertEquals "period 3 for 2011-01-02", 3, periodCounter.belongsToPeriod(date20110102)

        shouldFail(BeforeSimulationStartException, { periodCounter.belongsToPeriod(date20080101)})
        shouldFail(AfterSimulationEndException, { periodCounter.belongsToPeriod(date20111231)})
        shouldFail(AfterSimulationEndException, { periodCounter.belongsToPeriod(new DateTime(2011,1,3,0,0,0,0))})
    }

    void testStartOfPeriod() {
        assertEquals "period 2 for 2010-01-01", date20090909, periodCounter.startOfPeriod(date20100101)
        assertEquals "period 2 for 2010-03-31", date20090909, periodCounter.startOfPeriod(date20100331)
        assertEquals "period 2 for 2010-12-31", date20090909, periodCounter.startOfPeriod(date20101231)
        assertEquals "period 2 for 2011-01-01", date20090909, periodCounter.startOfPeriod(date20110101)
        assertEquals "period 3 for 2011-01-02", date20110102, periodCounter.startOfPeriod(date20110102)
        shouldFail(AfterSimulationEndException, { periodCounter.startOfPeriod(date20121231) })

        assertEquals "start of period 0", date20090101, periodCounter.startOfPeriod(0)
        assertEquals "start of period 1", date20090325, periodCounter.startOfPeriod(1)
        assertEquals "start of period 2", date20090909, periodCounter.startOfPeriod(2)
        assertEquals "start of period 3", date20110102, periodCounter.startOfPeriod(3)
        shouldFail(NotInProjectionHorizon, { periodCounter.startOfPeriod(4) })
    }

    void testEndOfPeriod() {
        assertEquals "period 0 for 2010-01-01", date20110102, periodCounter.endOfPeriod(date20100101)
        assertEquals "period 0 for 2010-03-31", date20110102, periodCounter.endOfPeriod(date20100331)
        assertEquals "period 0 for 2010-12-31", date20110102, periodCounter.endOfPeriod(date20101231)
        assertEquals "period 1 for 2011-01-01", date20110102, periodCounter.endOfPeriod(date20110101)
        shouldFail(AfterSimulationEndException, { periodCounter.endOfPeriod(date20110102) })

        assertEquals "end of period 0", date20090325, periodCounter.endOfPeriod(0)
        assertEquals "end of period 1", date20090909, periodCounter.endOfPeriod(1)
        assertEquals "end of period 2", date20110102, periodCounter.endOfPeriod(2)
        shouldFail(AfterSimulationEndException, { periodCounter.endOfPeriod(3) })
    }

    void testStartOfFirstPeriod() {
        assertEquals "start of projection horizon", date20090101, periodCounter.startOfFirstPeriod()
    }

    void testEndOfLastPeriod() {
        assertEquals "end of projection horizon", date20110102, periodCounter.endOfLastPeriod()
    }
}
