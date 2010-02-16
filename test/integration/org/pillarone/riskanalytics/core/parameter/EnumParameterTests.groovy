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

}