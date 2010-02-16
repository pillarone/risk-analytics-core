package org.pillarone.riskanalytics.core.parameter

import org.joda.time.DateTime

class DateParameterTests extends GroovyTestCase {

    void testInsert() {
        DateParameter parameter = new DateParameter(path: "path", dateValue: new DateTime(1972, 6, 14, 0, 0, 0, 0))

        DateParameter savedParam = parameter.save()
        assertNotNull(savedParam)
        assertNotNull(savedParam.id)

    }

    void testGetInstance() {
        DateParameter parameter = new DateParameter(path: "path", dateValue: new DateTime(1972, 6, 14, 0, 0, 0, 0))
        assertEquals new DateTime(1972, 6, 14, 0, 0, 0, 0), parameter.dateValue
    }

    void testSetInstance() {
        DateParameter parameter = new DateParameter(path: "path", dateValue: new DateTime(1972, 6, 14, 0, 0, 0, 0))
        assertEquals new DateTime(1972, 6, 14, 0, 0, 0, 0), parameter.dateValue

        parameter.dateValue = new DateTime(2009, 4, 3, 0, 0, 0, 0)
        assertEquals new DateTime(2009, 4, 3, 0, 0, 0, 0), parameter.dateValue

    }
}
