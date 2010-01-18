package org.pillarone.riskanalytics.core.parameter

class DoubleParameterTests extends GroovyTestCase {

    void testInsert() {
        DoubleParameter parameter = new DoubleParameter(path:"path", doubleValue:1.2)

        DoubleParameter savedParam = parameter.save()
        assertNotNull(savedParam)
        assertNotNull(savedParam.id)

    }
    
    void testGetInstance() {
        DoubleParameter parameter = new DoubleParameter(path:"path", doubleValue:1.2)
        assertEquals 1.2, parameter.getParameterInstance()
    }
    
    void testSetInstance() {
        DoubleParameter parameter = new DoubleParameter(path:"path", doubleValue:0.0)
        assertEquals 0.0, parameter.doubleValue

        parameter.setParameterInstance(0.1)
        assertEquals 0.1, parameter.doubleValue

        parameter.setParameterInstance(0.2d)
        assertEquals 0.2d, parameter.doubleValue

        parameter.setParameterInstance(new Double(1.2))
        assertEquals new Double(1.2), parameter.doubleValue

        parameter.parameterInstance = new Double(2.2)
        assertEquals new Double(2.2), parameter.doubleValue

        parameter.parameterInstance = 3.2
        assertEquals 3.2, parameter.doubleValue

        parameter.parameterInstance = 4.2d
        assertEquals 4.2d, parameter.doubleValue
        
        parameter.setParameterInstance(new Integer(1))
    }
}
