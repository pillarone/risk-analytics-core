package org.pillarone.riskanalytics.core.simulation

import org.joda.time.DateTime


class VariableLengthPeriodCounterTests extends GroovyTestCase {

    VariableLengthPeriodCounter periodCounter
    List<DateTime> dates

    void setUp() {
        dates = [
                new DateTime(2009, 1, 1, 0, 0, 0, 0),
                new DateTime(2009, 3, 25, 0, 0, 0, 0),
                new DateTime(2009, 9, 9, 0, 0, 0, 0),
                new DateTime(2011, 1, 2, 0, 0, 0, 0)
        ]
        periodCounter = new VariableLengthPeriodCounter(dates)
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
        assertEquals dates[1].minusDays(1).millis, periodCounter.currentPeriodEnd.millis
        periodCounter++
        assertEquals dates[2].minusDays(1).millis, periodCounter.currentPeriodEnd.millis
        periodCounter++
        assertEquals dates[3].minusDays(1).millis, periodCounter.currentPeriodEnd.millis
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
}
