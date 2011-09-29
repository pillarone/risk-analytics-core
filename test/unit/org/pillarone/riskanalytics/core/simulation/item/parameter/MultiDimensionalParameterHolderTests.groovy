package org.pillarone.riskanalytics.core.simulation.item.parameter

import org.pillarone.riskanalytics.core.parameterization.ConstrainedMultiDimensionalParameter
import org.pillarone.riskanalytics.core.example.parameter.ExampleMultiDimensionalConstraints
import org.pillarone.riskanalytics.core.parameterization.IMultiDimensionalConstraints
import org.pillarone.riskanalytics.core.example.marker.ITest2ComponentMarker
import org.pillarone.riskanalytics.core.example.marker.ITestComponentMarker
import org.pillarone.riskanalytics.core.parameterization.ComboBoxTableMultiDimensionalParameter

/**
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
class MultiDimensionalParameterHolderTests extends GroovyTestCase {

    void testReferencePaths_CMDP() {
        IMultiDimensionalConstraints markerConstraint = new ExampleMultiDimensionalConstraints()
        ConstrainedMultiDimensionalParameter parameter = new ConstrainedMultiDimensionalParameter(
                [['example output component', 'hierarchy output component'], [1.0, 0.0]], ['component', 'values'], markerConstraint
        )
        MultiDimensionalParameterHolder parameterHolder = new MultiDimensionalParameterHolder('europe:suisse', 0, parameter)
        assertEquals 'one reference path found', ['europe:suisse'], parameterHolder.referencePaths(ITestComponentMarker, 'hierarchy output component')
        assertEquals 'hierarchy output component', ['europe:suisse'], parameterHolder.referencePaths(ITestComponentMarker, 'example output component')
        assertEquals 'reference not found due to wrong marker interface', [], parameterHolder.referencePaths(ITest2ComponentMarker, 'hierarchy output component')
        assertEquals 'reference not found due to wrong value', [], parameterHolder.referencePaths(ITestComponentMarker, '1.0')
    }

    void testReferencePaths_CBTMDP() {
        ComboBoxTableMultiDimensionalParameter parameter = new ComboBoxTableMultiDimensionalParameter(
                ['example output component', 'hierarchy output component'], ['component'], ITestComponentMarker
        )
        MultiDimensionalParameterHolder parameterHolder = new MultiDimensionalParameterHolder('europe:suisse', 0, parameter)
        assertEquals 'one reference path found', ['europe:suisse'], parameterHolder.referencePaths(ITestComponentMarker, 'hierarchy output component')
        assertEquals 'hierarchy output component', ['europe:suisse'], parameterHolder.referencePaths(ITestComponentMarker, 'example output component')
        assertEquals 'reference not found due to wrong marker interface', [], parameterHolder.referencePaths(ITest2ComponentMarker, 'hierarchy output component')
        assertEquals 'reference not found due to wrong value', [], parameterHolder.referencePaths(ITestComponentMarker, '1.0')
    }


    void testUpdateReferenceValues_CMDP() {
        IMultiDimensionalConstraints markerConstraint = new ExampleMultiDimensionalConstraints()
        ConstrainedMultiDimensionalParameter parameter = new ConstrainedMultiDimensionalParameter(
                [['example output component', 'hierarchy output component'], [1.0, 0.0]], ['component', 'values'], markerConstraint
        )
        MultiDimensionalParameterHolder parameterHolder = new MultiDimensionalParameterHolder('europe:suisse', 0, parameter)
        assertFalse(parameterHolder.modified)
        assertEquals 'one reference path found', ['europe:suisse'], parameterHolder.updateReferenceValues(ITestComponentMarker, 'hierarchy output component', 'flat output component')
        assertTrue(parameterHolder.modified)
        assertEquals 'correct modification hierarchy -> flat', 'flat output component', parameterHolder.getBusinessObject().getValueAt(2, 0)
        assertEquals 'one reference path found', ['europe:suisse'], parameterHolder.updateReferenceValues(ITestComponentMarker, 'example output component', 'output component')
        assertEquals 'correct modification example -> flat', 'output component', parameterHolder.getBusinessObject().getValueAt(1, 0)
        assertEquals 'reference not found due to wrong marker interface', [], parameterHolder.updateReferenceValues(ITest2ComponentMarker, 'hierarchy output component', 'flat output component')
        assertEquals 'reference not found due to wrong value', [], parameterHolder.updateReferenceValues(ITestComponentMarker, '1.0', '5.0')
    }

    void testUpdateReferenceValues_CBTMDP() {
        ComboBoxTableMultiDimensionalParameter parameter = new ComboBoxTableMultiDimensionalParameter(
                ['example output component', 'hierarchy output component'], ['component'], ITestComponentMarker
        )
        MultiDimensionalParameterHolder parameterHolder = new MultiDimensionalParameterHolder('europe:suisse', 0, parameter)
        assertFalse(parameterHolder.modified)
        assertEquals 'one reference path found', ['europe:suisse'], parameterHolder.updateReferenceValues(ITestComponentMarker, 'hierarchy output component', 'flat output component')
        assertTrue(parameterHolder.modified)
        assertEquals 'correct modification hierarchy -> flat', 'flat output component', parameterHolder.getBusinessObject().getValueAt(2, 0)
        assertEquals 'one reference path found', ['europe:suisse'], parameterHolder.updateReferenceValues(ITestComponentMarker, 'example output component', 'output component')
        assertEquals 'correct modification example -> flat', 'output component', parameterHolder.getBusinessObject().getValueAt(1, 0)
        assertEquals 'reference not found due to wrong marker interface', [], parameterHolder.updateReferenceValues(ITest2ComponentMarker, 'hierarchy output component', 'flat output component')
        assertEquals 'reference not found due to wrong value', [], parameterHolder.updateReferenceValues(ITestComponentMarker, '1.0', '5.0')
    }

    void testClone() {
        MultiDimensionalParameterHolder holder = ParameterHolderFactory.getHolder("europe:suisee", 0, new ComboBoxTableMultiDimensionalParameter(
                ['example output component', 'hierarchy output component'], ['component'], ITestComponentMarker))

        def clone = holder.clone()
        assertFalse clone.added
        assertFalse clone.modified
        assertFalse clone.removed
    }
}
