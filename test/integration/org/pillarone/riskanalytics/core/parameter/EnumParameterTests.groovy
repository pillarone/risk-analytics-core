package org.pillarone.riskanalytics.core.parameter

import org.junit.Test

import static org.junit.Assert.assertNotNull

class EnumParameterTests {


    @Test
    void testInsert() {
        EnumParameter parameter = new EnumParameter(path: "path", parameterValue: "FIRST_VALUE", parameterType: "org.pillarone.riskanalytics.core.example.parameter.ExampleEnum")
        EnumParameter savedParam = parameter.save()
        assertNotNull(savedParam)
        assertNotNull(savedParam.id)

    }

}