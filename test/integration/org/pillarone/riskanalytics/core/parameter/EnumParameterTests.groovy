package org.pillarone.riskanalytics.core.parameter

import org.pillarone.riskanalytics.core.example.parameter.ExampleEnum
import org.pillarone.riskanalytics.core.example.parameter.ExampleParameterObjectClassifier

class EnumParameterTests extends GroovyTestCase {


    void testInsert() {
        EnumParameter parameter = new EnumParameter(path: "path", parameterValue: "FIRST_VALUE", parameterType: "org.pillarone.riskanalytics.core.example.parameter.ExampleEnum")
        EnumParameter savedParam = parameter.save()
        assertNotNull(savedParam)
        assertNotNull(savedParam.id)

    }

    void testGetInstance() {
        EnumParameter parameter = new EnumParameter(path: "path", parameterValue: "FIRST_VALUE", parameterType: "org.pillarone.riskanalytics.core.example.parameter.ExampleEnum")
        assertSame ExampleEnum.FIRST_VALUE, parameter.getParameterInstance()

        EnumParameter distType = new EnumParameter(path: "type", parameterValue: "TYPE0", parameterType: "org.pillarone.riskanalytics.core.example.parameter.ExampleParameterObjectClassifier")
        assertSame ExampleParameterObjectClassifier.TYPE0, distType.getParameterInstance()

    }

    void testSetInstance() {
        EnumParameter parameter = new EnumParameter(path: "path", parameterValue: "FIRST_VALUE", parameterType: "org.pillarone.riskanalytics.core.example.parameter.ExampleEnum")

        assertSame ExampleEnum.FIRST_VALUE, parameter.getParameterInstance()

        parameter.setParameterInstance(ExampleEnum.SECOND_VALUE)

        assertSame ExampleEnum.SECOND_VALUE, parameter.getParameterInstance()
        assertEquals "SECOND_VALUE", parameter.parameterValue
        assertEquals "org.pillarone.riskanalytics.core.example.parameter.ExampleEnum", parameter.parameterType
    }

}