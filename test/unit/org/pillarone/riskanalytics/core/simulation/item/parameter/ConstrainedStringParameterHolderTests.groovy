package org.pillarone.riskanalytics.core.simulation.item.parameter

import org.pillarone.riskanalytics.core.parameterization.ConstrainedString
import org.pillarone.riskanalytics.core.example.marker.ITestComponentMarker
import org.pillarone.riskanalytics.core.example.marker.ITest2ComponentMarker

/**
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
class ConstrainedStringParameterHolderTests extends GroovyTestCase {

    void testReferencePaths() {
        ConstrainedString constrainedString = new ConstrainedString(ITestComponentMarker, 'apple')
        ConstrainedStringParameterHolder parameterHolder = new ConstrainedStringParameterHolder('europe:suisse', 0, constrainedString)
        assertEquals 'one reference path found', ['europe:suisse'], parameterHolder.referencePaths(ITestComponentMarker, 'apple')
        assertEquals 'reference not found', [], parameterHolder.referencePaths(ITestComponentMarker, 'cherry')
        assertEquals 'reference not found, wrong marker', [], parameterHolder.referencePaths(ITest2ComponentMarker, 'apple')
    }

    void testUpdateReferenceValues() {
        ConstrainedString constrainedString = new ConstrainedString(ITestComponentMarker, 'apple')
        ConstrainedStringParameterHolder parameterHolder = new ConstrainedStringParameterHolder('europe:suisse', 0, constrainedString)
        assertEquals 'verify original value', 'apple', parameterHolder.getBusinessObject().stringValue

        assertEquals 'one reference path found', ['europe:suisse'], parameterHolder.updateReferenceValues(ITestComponentMarker, 'apple', 'banana')
        assertEquals 'correct modification: apple -> banana', 'banana', parameterHolder.getBusinessObject().stringValue

        assertEquals 'reference not found', [], parameterHolder.updateReferenceValues(ITestComponentMarker, 'apple', 'cherry')
        assertEquals 'correct modification: apple -> cherry', 'banana', parameterHolder.getBusinessObject().stringValue

        assertEquals 'reference banana found', ['europe:suisse'], parameterHolder.updateReferenceValues(ITestComponentMarker, 'banana', 'cherry')
        assertEquals 'correct modification: banana -> cherry', 'cherry', parameterHolder.getBusinessObject().stringValue

        assertEquals 'reference not found, wrong marker', [], parameterHolder.updateReferenceValues(ITest2ComponentMarker, 'banana', 'apple')
        assertEquals 'reference not found, banana -> apple', 'cherry', parameterHolder.getBusinessObject().stringValue
    }
}
