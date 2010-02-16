package org.pillarone.riskanalytics.core.parameter

class StringParameterTests extends GroovyTestCase {

    void testInsert() {
        StringParameter parameter = new StringParameter(path: "path", parameterValue: 'string')

        StringParameter savedParam = parameter.save()
        assertNotNull(savedParam)
        assertNotNull(savedParam.id)

    }

}
