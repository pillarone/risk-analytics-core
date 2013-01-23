package org.pillarone.riskanalytics.core.parameter

class BooleanParameterTests extends GroovyTestCase {

    void testInsert() {
        BooleanParameter parameter = new BooleanParameter(path: "path", booleanValue: true)

        BooleanParameter savedParam = parameter.save()
        assertNotNull(savedParam)
        assertNotNull(savedParam.id)

    }

    void testGetInstance() {
        BooleanParameter parameter = new BooleanParameter(path: "path", booleanValue: true)
        assertEquals true, parameter.booleanValue
    }

    void testSetInstance() {
        BooleanParameter parameter = new BooleanParameter(path: "path", booleanValue: false)
        assertEquals false, parameter.booleanValue

        parameter.booleanValue = true
        assertEquals true, parameter.booleanValue
    }
}