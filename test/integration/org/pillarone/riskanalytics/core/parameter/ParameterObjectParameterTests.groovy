package org.pillarone.riskanalytics.core.parameter

import org.pillarone.riskanalytics.core.example.parameter.ExampleParameterObjectClassifier
import org.pillarone.riskanalytics.core.parameterization.IParameterObject

class ParameterObjectParameterTests extends GroovyTestCase {

    void testInsert() {
        EnumParameter type = new EnumParameter(path: "type")
        type.parameterType = ExampleParameterObjectClassifier.name
        type.parameterValue = ExampleParameterObjectClassifier.TYPE0.toString()
        DoubleParameter lambda = new DoubleParameter(path: "a")
        lambda.doubleValue = 1.1d
        ParameterEntry parameterEntry = new ParameterEntry(path: "entry", parameterEntryKey: "a", parameterEntryValue: lambda)

        ParameterObjectParameter parameter = new ParameterObjectParameter(path: "path")
        parameter.type = type
        parameter.addToParameterEntries(parameterEntry)

        def savedParam = parameter.save()
        assertNotNull(savedParam)
        assertNotNull(savedParam.id)

        parameter.discard()
        def reloaded = ParameterObjectParameter.findByPath('path')
        assertEquals 1, reloaded.parameterEntries.size()
    }

}
