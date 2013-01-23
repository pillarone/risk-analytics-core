package org.pillarone.riskanalytics.core.persistence

import org.pillarone.riskanalytics.core.test.DateTimeMillisUserTypeTest
import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.parameter.DateParameter

class DateTimeMillisUserTypeTests extends GroovyTestCase {

    void testSave() {
        DateTime date = new DateTime(2010, 1, 1, 0, 0, 0, 0)
        DateTimeMillisUserTypeTest time = new DateTimeMillisUserTypeTest(theDate: date)
        time.save(flush: true)

        time.discard()

        long id = time.id

        DateTimeMillisUserTypeTest time2 = DateTimeMillisUserTypeTest.get(id)
        assertEquals date, time2.theDate
    }

    void testNull() {
        DateTimeMillisUserTypeTest time = new DateTimeMillisUserTypeTest(theDate: null)
        time.save(flush: true)

        time.discard()

        long id = time.id

        DateTimeMillisUserTypeTest time2 = DateTimeMillisUserTypeTest.get(id)
        assertNull time2.theDate
    }

    void testUpdate() {
        DateTime date = new DateTime(2010, 1, 1, 0, 0, 0, 0)
        DateTimeMillisUserTypeTest time = new DateTimeMillisUserTypeTest(theDate: date)
        time.save(flush: true)

        time.discard()

        long id = time.id

        time = DateTimeMillisUserTypeTest.get(id)
        assertEquals date, time.theDate

        date = new DateTime(2011, 1, 1, 0, 0, 0, 0)
        time.theDate = date

        time.save(flush: true)

        time.discard()

        time = DateTimeMillisUserTypeTest.get(id)
        assertEquals date, time.theDate
    }

}
