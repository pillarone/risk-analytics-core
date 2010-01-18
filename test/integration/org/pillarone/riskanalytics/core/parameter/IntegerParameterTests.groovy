package org.pillarone.riskanalytics.core.parameter

class IntegerParameterTests extends GroovyTestCase {

    void testObjectCreation() {
        IntegerParameter parameter = new IntegerParameter(path: "path", integerValue: 1)
        assertEquals 1, parameter.getParameterInstance()
        parameter = new IntegerParameter(path: "path", parameterInstance: new Integer(2))
        int instance = parameter.getParameterInstance()
        assertEquals new Integer(2), instance
        parameter = new IntegerParameter(path: "path", parameterInstance: 3)
        assertEquals 3, parameter.getParameterInstance()
    }

    void testInsert() {
        IntegerParameter parameter = new IntegerParameter(path: "path", integerValue: 2)

        IntegerParameter savedParam = parameter.save()
        assertNotNull(savedParam)
        assertNotNull(savedParam.id)

    }

    void testGetInstance() {
        IntegerParameter parameter = new IntegerParameter(path: "path", integerValue: 2)
        assertEquals 2, parameter.getParameterInstance()
    }

    void testSetInstance() {
        IntegerParameter parameter = new IntegerParameter(path: "path", integerValue: 1)
        assertEquals 1, parameter.integerValue

        parameter.setParameterInstance(new Integer(2))
        assertEquals 2, parameter.integerValue

        parameter.setParameterInstance(3)
        assertEquals 3, parameter.integerValue

    }
}
