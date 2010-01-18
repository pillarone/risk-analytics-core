package org.pillarone.riskanalytics.core.parameter

import org.pillarone.riskanalytics.core.example.parameter.ExampleParameterObjectClassifier
import org.pillarone.riskanalytics.core.parameterization.IParameterObject

class ParameterObjectParameterTests extends GroovyTestCase {

    void testInsert() {
        EnumParameter type = new EnumParameter(path: "type")
        type.parameterInstance = ExampleParameterObjectClassifier.TYPE0
        DoubleParameter lambda = new DoubleParameter(path: "a")
        lambda.parameterInstance = 1.1d
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

    void testParameterMap() {
        EnumParameter type = new EnumParameter(path: "type")
        type.parameterInstance = ExampleParameterObjectClassifier.TYPE0

        DoubleParameter mean = new DoubleParameter(path: "a")
        mean.parameterInstance = 20d

        DoubleParameter stDev = new DoubleParameter(path: "b")
        stDev.parameterInstance = 2d

        ParameterEntry meanParameterEntry = new ParameterEntry(parameterEntryKey: "a", parameterEntryValue: mean)
        ParameterEntry stDevParameterEntry = new ParameterEntry(parameterEntryKey: "b", parameterEntryValue: stDev)

        ParameterObjectParameter parameter = new ParameterObjectParameter(path: "path")
        parameter.type = type
        parameter.addToParameterEntries(meanParameterEntry)
        parameter.addToParameterEntries(stDevParameterEntry)

        Map parameterMap = parameter.parameterMap()
        assertEquals 2, parameterMap.keySet().size()
        assertEquals 20d, parameterMap["a"]
        assertEquals 2d, parameterMap["b"]
    }

    void testGetParameterInstance() {
        EnumParameter type = new EnumParameter(path: "type")
        type.parameterInstance = ExampleParameterObjectClassifier.TYPE0

        DoubleParameter mean = new DoubleParameter(path: "a")
        mean.parameterInstance = 20d

        DoubleParameter stDev = new DoubleParameter(path: "b")
        stDev.parameterInstance = 2d

        ParameterEntry meanParameterEntry = new ParameterEntry(parameterEntryKey: "a", parameterEntryValue: mean)
        ParameterEntry stDevParameterEntry = new ParameterEntry(parameterEntryKey: "b", parameterEntryValue: stDev)

        ParameterObjectParameter parameter = new ParameterObjectParameter(path: "path")
        parameter.type = type
        parameter.addToParameterEntries(meanParameterEntry)
        parameter.addToParameterEntries(stDevParameterEntry)

        IParameterObject distribution = parameter.getParameterInstance()
        assertNotNull distribution
        assertSame ExampleParameterObjectClassifier.TYPE0, distribution.type
        assertEquals 20d, distribution.parameters["a"]
        assertEquals 2d, distribution.parameters["b"]

    }

    void testSetParameterInstance() {
        def distribution = ExampleParameterObjectClassifier.TYPE0.getParameterObject(["a": 1d, "b": 2d])
        ParameterObjectParameter parameter = new ParameterObjectParameter(path: "path")
        parameter.setParameterInstance(distribution)

        assertSame ExampleParameterObjectClassifier.TYPE0, parameter.type.parameterInstance
        assertEquals 1d, parameter.parameterMap()["a"]
        assertEquals 2d, parameter.parameterMap()["b"]

        parameter = new ParameterObjectParameter(path: "path", periodIndex: 0)
        parameter.setParameterInstance(ExampleParameterObjectClassifier.TYPE0, ExampleParameterObjectClassifier.TYPE0.parameters)

        assertSame ExampleParameterObjectClassifier.TYPE0, parameter.type.parameterInstance
        assertEquals 10d, parameter.parameterMap()["a"]
        assertEquals 100d, parameter.parameterMap()["b"]

        assertEquals parameter.periodIndex, parameter.type.periodIndex
        parameter.parameterEntries.each {
            assertEquals parameter.periodIndex, it.parameterEntryValue.periodIndex
        }
    }
}
