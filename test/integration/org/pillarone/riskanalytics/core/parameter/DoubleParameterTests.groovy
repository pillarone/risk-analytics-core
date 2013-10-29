package org.pillarone.riskanalytics.core.parameter

import org.junit.Test

import static org.junit.Assert.*

class DoubleParameterTests {

    @Test
    void testInsert() {
        DoubleParameter parameter = new DoubleParameter(path: "path", doubleValue: 1.2)

        DoubleParameter savedParam = parameter.save()
        assertNotNull(savedParam)
        assertNotNull(savedParam.id)

    }

    @Test
    void testGetInstance() {
        DoubleParameter parameter = new DoubleParameter(path: "path", doubleValue: 1.2)
        assertEquals 1.2, parameter.doubleValue, 0
    }

    @Test
    void testSetInstance() {
        DoubleParameter parameter = new DoubleParameter(path: "path", doubleValue: 0.0)
        assertEquals 0.0, parameter.doubleValue, 0

        parameter.doubleValue = 0.1
        assertEquals 0.1, parameter.doubleValue, 0

        parameter.doubleValue = 0.2d
        assertEquals 0.2d, parameter.doubleValue, 0

        parameter.doubleValue = new Double(1.2)
        assertEquals new Double(1.2), parameter.doubleValue, 0

        parameter.doubleValue = new Double(2.2)
        assertEquals new Double(2.2), parameter.doubleValue, 0

        parameter.doubleValue = 3.2
        assertEquals 3.2, parameter.doubleValue, 0

        parameter.doubleValue = 4.2d
        assertEquals 4.2d, parameter.doubleValue, 0
    }
}
