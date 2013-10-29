package org.pillarone.riskanalytics.core.simulation.item.parameter

import org.junit.Test
import org.pillarone.riskanalytics.core.parameter.DoubleParameter
import org.pillarone.riskanalytics.core.parameter.EnumParameter
import org.pillarone.riskanalytics.core.parameter.Parameter
import org.pillarone.riskanalytics.core.parameter.ParameterEntry
import org.pillarone.riskanalytics.core.parameter.ParameterObjectParameter
import org.pillarone.riskanalytics.core.example.parameter.ExampleParameterObjectClassifier
import org.pillarone.riskanalytics.core.parameter.StringParameter
import org.pillarone.riskanalytics.core.example.parameter.ExampleParameterObject
import org.pillarone.riskanalytics.core.example.marker.ITestComponentMarker
import org.pillarone.riskanalytics.core.example.marker.ITest2ComponentMarker
import org.pillarone.riskanalytics.core.parameterization.ConstrainedString
import org.pillarone.riskanalytics.core.example.parameter.ExampleMultiDimensionalConstraints
import org.pillarone.riskanalytics.core.parameterization.ComboBoxTableMultiDimensionalParameter

import static org.junit.Assert.*

class ParameterObjectParameterHolderTests {

    @Test
    void testSimpleApply() {
        ParameterObjectParameter parameter = new ParameterObjectParameter(path: 'path', periodIndex: 0)
        parameter.type = new EnumParameter(parameterType: ExampleParameterObjectClassifier.getName(), parameterValue: "TYPE1", path: 'path', periodIndex: 0)

        parameter.addToParameterEntries(new ParameterEntry(parameterEntryKey: "p1", parameterEntryValue: new DoubleParameter(doubleValue: 1, path: 'path', periodIndex: 0)))
        parameter.addToParameterEntries(new ParameterEntry(parameterEntryKey: "p2", parameterEntryValue: new DoubleParameter(doubleValue: 0.1, path: 'path', periodIndex: 0)))

        assertNotNull parameter.save(flush: true)

        int paramCount = Parameter.count()
        int entryCount = ParameterEntry.count()

        ParameterObjectParameterHolder holder = new ParameterObjectParameterHolder(parameter)

        assertEquals ExampleParameterObjectClassifier.TYPE1, holder.classifier
        assertEquals 2, holder.classifierParameters.size()

        ParameterHolder p1 = holder.classifierParameters.find {entry -> entry.key == "p1"}.value
        assertNotNull p1

        assertEquals 1d, p1.businessObject, 0d

        ParameterHolder p2 = holder.classifierParameters.find {entry -> entry.key == "p2"}.value
        assertNotNull p2

        assertEquals 0.1, p2.businessObject, 0d

        assertFalse holder.hasParameterChanged()
        p1.value = 2
        p2.value = 0.2
        assertTrue holder.hasParameterChanged()

        holder.applyToDomainObject(parameter)

        parameter.save(flush: true)

        assertEquals paramCount, Parameter.count()
        assertEquals entryCount, ParameterEntry.count()

        holder = new ParameterObjectParameterHolder(Parameter.get(parameter.id))

        assertEquals ExampleParameterObjectClassifier.TYPE1, holder.classifier
        assertEquals 2, holder.classifierParameters.size()

        p1 = holder.classifierParameters.find {entry -> entry.key == "p1"}.value
        assertNotNull p1

        assertEquals 2, p1.businessObject, 0

        p2 = holder.classifierParameters.find {entry -> entry.key == "p2"}.value
        assertNotNull p2

        assertEquals 0.2, p2.businessObject, 0

    }

    @Test
    void testApplyWithLessEntries() {
        ParameterObjectParameter parameter = new ParameterObjectParameter(path: 'path', periodIndex: 0)
        parameter.type = new EnumParameter(parameterType: ExampleParameterObjectClassifier.getName(), parameterValue: "TYPE2", path: 'path', periodIndex: 0)

        parameter.addToParameterEntries(new ParameterEntry(parameterEntryKey: "p1", parameterEntryValue: new DoubleParameter(doubleValue: 1, path: 'path', periodIndex: 0)))
        parameter.addToParameterEntries(new ParameterEntry(parameterEntryKey: "p2", parameterEntryValue: new DoubleParameter(doubleValue: 0.1, path: 'path', periodIndex: 0)))
        parameter.addToParameterEntries(new ParameterEntry(parameterEntryKey: "p3", parameterEntryValue: new DoubleParameter(doubleValue: 0.01, path: 'path', periodIndex: 0)))

        assertNotNull parameter.save(flush: true)

        int paramCount = Parameter.count()
        int entryCount = ParameterEntry.count()

        ParameterObjectParameterHolder holder = new ParameterObjectParameterHolder(parameter)

        assertEquals ExampleParameterObjectClassifier.TYPE2, holder.classifier
        assertEquals 3, holder.classifierParameters.size()

        holder.value = "TYPE1"

        holder.applyToDomainObject(parameter)

        parameter.save(flush: true)

        assertEquals paramCount - 1, Parameter.count()
        assertEquals entryCount - 1, ParameterEntry.count()

        holder = new ParameterObjectParameterHolder(Parameter.get(parameter.id))

        assertEquals ExampleParameterObjectClassifier.TYPE1, holder.classifier
        assertEquals 2, holder.classifierParameters.size()

        assertNotNull holder.classifierParameters.find {entry -> entry.key == "p1"}
        assertNotNull holder.classifierParameters.find {entry -> entry.key == "p2"}
        assertNull holder.classifierParameters.find {entry -> entry.key == "p3"}
    }

    @Test
    void testApplyWithAdditionalEntries() {
        ParameterObjectParameter parameter = new ParameterObjectParameter(path: 'path', periodIndex: 0)
        parameter.type = new EnumParameter(parameterType: ExampleParameterObjectClassifier.getName(), parameterValue: "TYPE1", path: 'path', periodIndex: 0)

        parameter.addToParameterEntries(new ParameterEntry(parameterEntryKey: "p1", parameterEntryValue: new DoubleParameter(doubleValue: 1, path: 'path', periodIndex: 0)))
        parameter.addToParameterEntries(new ParameterEntry(parameterEntryKey: "p2", parameterEntryValue: new DoubleParameter(doubleValue: 0.1, path: 'path', periodIndex: 0)))

        assertNotNull parameter.save(flush: true)

        int paramCount = Parameter.count()
        int entryCount = ParameterEntry.count()

        ParameterObjectParameterHolder holder = new ParameterObjectParameterHolder(parameter)

        assertEquals ExampleParameterObjectClassifier.TYPE1, holder.classifier
        assertEquals 2, holder.classifierParameters.size()

        holder.value = "TYPE2"

        holder.applyToDomainObject(parameter)

        parameter.save(flush: true)

        assertEquals paramCount + 1, Parameter.count()
        assertEquals entryCount + 1, ParameterEntry.count()

        holder = new ParameterObjectParameterHolder(Parameter.get(parameter.id))

        assertEquals ExampleParameterObjectClassifier.TYPE2, holder.classifier
        assertEquals 3, holder.classifierParameters.size()

        assertNotNull holder.classifierParameters.find {entry -> entry.key == "p1"}.value
        assertNotNull holder.classifierParameters.find {entry -> entry.key == "p2"}.value
        assertNotNull holder.classifierParameters.find {entry -> entry.key == "p3"}.value

    }

    @Test
    void testUpdateValue() {
        ParameterObjectParameter parameter = new ParameterObjectParameter(path: 'path', periodIndex: 0)
        parameter.type = new EnumParameter(parameterType: ExampleParameterObjectClassifier.getName(), parameterValue: "TYPE1", path: 'path', periodIndex: 0)

        parameter.addToParameterEntries(new ParameterEntry(parameterEntryKey: "p1", parameterEntryValue: new DoubleParameter(doubleValue: 1, path: 'path', periodIndex: 0)))
        parameter.addToParameterEntries(new ParameterEntry(parameterEntryKey: "p2", parameterEntryValue: new DoubleParameter(doubleValue: 0.1, path: 'path', periodIndex: 0)))

        ParameterObjectParameterHolder holder = new ParameterObjectParameterHolder(parameter)
        holder.value = "TYPE0"

        assertEquals ExampleParameterObjectClassifier.TYPE0, holder.classifier
        assertEquals 2, holder.classifierParameters.size()
        assertTrue holder.classifierParameters.containsKey("a")
        assertEquals 10d, holder.classifierParameters.get("a").businessObject, 0d

        assertTrue holder.classifierParameters.containsKey("b")
        assertEquals 100, holder.classifierParameters.get("b").businessObject, 0d
    }

    @Test
    void testUpdateValueKeepExistingValues() {
        ParameterObjectParameter parameter = new ParameterObjectParameter(path: 'path', periodIndex: 0)
        parameter.type = new EnumParameter(parameterType: ExampleParameterObjectClassifier.getName(), parameterValue: "TYPE1", path: 'path', periodIndex: 0)

        parameter.addToParameterEntries(new ParameterEntry(parameterEntryKey: "p1", parameterEntryValue: new DoubleParameter(doubleValue: 22, path: 'path', periodIndex: 0)))
        parameter.addToParameterEntries(new ParameterEntry(parameterEntryKey: "p2", parameterEntryValue: new DoubleParameter(doubleValue: 33, path: 'path', periodIndex: 0)))

        ParameterObjectParameterHolder holder = new ParameterObjectParameterHolder(parameter)
        holder.value = "TYPE2"

        assertEquals ExampleParameterObjectClassifier.TYPE2, holder.classifier
        assertEquals 3, holder.classifierParameters.size()

        assertTrue holder.classifierParameters.containsKey("p1")
        assertEquals 22d, holder.classifierParameters.get("p1").businessObject, 0d

        assertTrue holder.classifierParameters.containsKey("p2")
        assertEquals 33, holder.classifierParameters.get("p2").businessObject, 0d

    }

    @Test
    void testUpdateValueReplaceExistingValuesWithDifferentTypes() {
        ParameterObjectParameter parameter = new ParameterObjectParameter(path: 'path', periodIndex: 0)
        parameter.type = new EnumParameter(parameterType: ExampleParameterObjectClassifier.getName(), parameterValue: "TYPE2", path: 'path', periodIndex: 0)

        parameter.addToParameterEntries(new ParameterEntry(parameterEntryKey: "p1", parameterEntryValue: new DoubleParameter(doubleValue: 22, path: 'path', periodIndex: 0)))
        parameter.addToParameterEntries(new ParameterEntry(parameterEntryKey: "p2", parameterEntryValue: new DoubleParameter(doubleValue: 33, path: 'path', periodIndex: 0)))
        parameter.addToParameterEntries(new ParameterEntry(parameterEntryKey: "p3", parameterEntryValue: new StringParameter(parameterValue: "33", path: 'path', periodIndex: 0)))

        ParameterObjectParameterHolder holder = new ParameterObjectParameterHolder(parameter)
        holder.value = "TYPE3"

        assertEquals ExampleParameterObjectClassifier.TYPE3, holder.classifier
        assertEquals 3, holder.classifierParameters.size()

        assertTrue holder.classifierParameters.containsKey("p1")
        assertEquals 22d, holder.classifierParameters.get("p1").businessObject, 0d

        assertTrue holder.classifierParameters.containsKey("p2")
        assertEquals 33, holder.classifierParameters.get("p2").businessObject, 0d

        assertTrue holder.classifierParameters.containsKey("p3")
        //should be the default value, because we don't want to keep the value if it has the same name, but is a different type
        assertEquals 2d, holder.classifierParameters.get("p3").businessObject, 0d

    }

    @Test
    void testHasParameterChanged() {
        ParameterObjectParameterHolder holder = ParameterHolderFactory.getHolder("path", 0, ExampleParameterObjectClassifier.getStrategy(ExampleParameterObjectClassifier.TYPE0, ["a": 0, "b": 1]))

        assertFalse holder.added
        assertFalse holder.removed
        assertFalse holder.hasParameterChanged()

        holder.classifierParameters.entrySet().toList()[0].value.setValue(5)

        assertTrue holder.hasParameterChanged()

        holder.removed = true

        assertFalse holder.hasParameterChanged()

    }
}