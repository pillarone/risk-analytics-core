package org.pillarone.riskanalytics.core.parameterization

import org.joda.time.DateTime

class DateTimeTests extends GroovyTestCase {

    void testShortConstructor() {
        DateTime dateTime = new DateTime(1972, 6, 14)
        assertNotNull dateTime
        assertEquals 1972, dateTime.year
        assertEquals 6, dateTime.monthOfYear
        assertEquals 14, dateTime.dayOfMonth
        assertEquals 0, dateTime.hourOfDay
        assertEquals 0, dateTime.minuteOfHour
        assertEquals 0, dateTime.secondOfMinute
        assertEquals 0, dateTime.millisOfSecond
    }
}