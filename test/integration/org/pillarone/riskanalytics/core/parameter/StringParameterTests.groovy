package org.pillarone.riskanalytics.core.parameter

import org.junit.Test

import static org.junit.Assert.assertNotNull

class StringParameterTests {

    @Test
    void testInsert() {
        StringParameter parameter = new StringParameter(path: "path", parameterValue: 'string')

        StringParameter savedParam = parameter.save()
        assertNotNull(savedParam)
        assertNotNull(savedParam.id)

    }

}
