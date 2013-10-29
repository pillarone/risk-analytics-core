package org.pillarone.riskanalytics.core.parameter

import org.junit.Test

import static org.junit.Assert.*

class BooleanParameterTests {

    @Test
    void testInsert() {
        BooleanParameter parameter = new BooleanParameter(path: "path", booleanValue: true)

        BooleanParameter savedParam = parameter.save()
        assertNotNull(savedParam)
        assertNotNull(savedParam.id)

    }

    @Test
    void testGetInstance() {
        BooleanParameter parameter = new BooleanParameter(path: "path", booleanValue: true)
        assertEquals true, parameter.booleanValue
    }

    @Test
    void testSetInstance() {
        BooleanParameter parameter = new BooleanParameter(path: "path", booleanValue: false)
        assertEquals false, parameter.booleanValue

        parameter.booleanValue = true
        assertEquals true, parameter.booleanValue
    }
}