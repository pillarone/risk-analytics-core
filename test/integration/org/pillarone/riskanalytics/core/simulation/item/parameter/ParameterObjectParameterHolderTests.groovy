package org.pillarone.riskanalytics.core.simulation.item.parameter

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

class ParameterObjectParameterHolderTests extends GroovyTestCase {

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

        assertEquals 1, p1.businessObject

        ParameterHolder p2 = holder.classifierParameters.find {entry -> entry.key == "p2"}.value
        assertNotNull p2

        assertEquals 0.1, p2.businessObject

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

        assertEquals 2, p1.businessObject

        p2 = holder.classifierParameters.find {entry -> entry.key == "p2"}.value
        assertNotNull p2

        assertEquals 0.2, p2.businessObject

    }

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
        assertEquals 10, holder.classifierParameters.get("a").businessObject

        assertTrue holder.classifierParameters.containsKey("b")
        assertEquals 100, holder.classifierParameters.get("b").businessObject
    }

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
        assertEquals 22, holder.classifierParameters.get("p1").businessObject

        assertTrue holder.classifierParameters.containsKey("p2")
        assertEquals 33, holder.classifierParameters.get("p2").businessObject

    }

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
        assertEquals 22, holder.classifierParameters.get("p1").businessObject

        assertTrue holder.classifierParameters.containsKey("p2")
        assertEquals 33, holder.classifierParameters.get("p2").businessObject

        assertTrue holder.classifierParameters.containsKey("p3")
        //should be the default value, because we don't want to keep the value if it has the same name, but is a different type
        assertEquals 2d, holder.classifierParameters.get("p3").businessObject

    }


    void testReferencePaths() {
        ExampleParameterObject parameterObject = new ExampleParameterObject(
                parameters : ExampleParameterObjectClassifier.getStrategy(ExampleParameterObjectClassifier.TYPE0,
                        [a: 100, b: 20]),
                classifier: ExampleParameterObjectClassifier.TYPE0)
        ParameterObjectParameterHolder parameterHolder = new ParameterObjectParameterHolder('x:y:z', 0, parameterObject)
        assertEquals 'a found', ['x:y:z'], parameterHolder.referencePaths(ITestComponentMarker, 'a')
        assertEquals 'reference not found', [], parameterHolder.referencePaths(ITestComponentMarker, 'cherry')
        assertEquals 'reference not found, wrong marker', [], parameterHolder.referencePaths(ITest2ComponentMarker, 'apple')
    }

    void testUpdateReferenceValues() {
        ConstrainedString constrainedString = new ConstrainedString(ITestComponentMarker, 'apple')
        ConstrainedStringParameterHolder parameterHolder = new ConstrainedStringParameterHolder('europe:suisse', 0, constrainedString)
        assertEquals 'verify original value', 'apple', parameterHolder.getBusinessObject().stringValue

        assertEquals 'one reference path found', ['europe:suisse'], parameterHolder.updateReferenceValues(ITestComponentMarker, 'apple', 'banana')
        assertEquals 'correct modification: apple -> banana', 'subBanana', parameterHolder.getBusinessObject().stringValue

        assertEquals 'reference not found', [], parameterHolder.updateReferenceValues(ITestComponentMarker, 'apple', 'cherry')
        assertEquals 'correct modification: apple -> cherry', 'subBanana', parameterHolder.getBusinessObject().stringValue

        assertEquals 'reference banana found', ['europe:suisse'], parameterHolder.updateReferenceValues(ITestComponentMarker, 'banana', 'cherry')
        assertEquals 'correct modification: banana -> cherry', 'subCherry', parameterHolder.getBusinessObject().stringValue

        assertEquals 'reference not found, wrong marker', [], parameterHolder.updateReferenceValues(ITest2ComponentMarker, 'banana', 'apple')
        assertEquals 'reference not found, banana -> apple', 'subCherry', parameterHolder.getBusinessObject().stringValue
    }

/*     List<String> referencePaths(Class markerInterface, String refValue) {
        if (classifierParameters.size() > 0) {
            for (ParameterHolder parameterHolder: classifierParameters.values()) {
                if (parameterHolder instanceof MultiDimensionalParameterHolder) {
                    return parameterHolder.referencePaths(markerInterface, refValue)
                }
            }
        }
        return Collections.emptyList()
    }

    List<String> updateReferenceValues(Class markerInterface, String oldValue, String newValue) {
        if (classifierParameters.size() > 0) {
            for (ParameterHolder parameterHolder: classifierParameters.values()) {
                if (parameterHolder instanceof MultiDimensionalParameterHolder) {
                    return parameterHolder.updateReferenceValues(markerInterface, oldValue, newValue)
                }
            }
        }
        return Collections.emptyList()
    }*/
}