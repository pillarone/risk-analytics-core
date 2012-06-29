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
                [['exampleOutputComponent', 'hierarchyOutputComponent'], [1.0, 0.0]], ['component', 'values'], markerConstraint
        )
        MultiDimensionalParameterHolder parameterHolder = new MultiDimensionalParameterHolder('europe:suisse', 0, parameter)
        assertEquals 'one reference path found', ['europe:suisse'], parameterHolder.referencePaths(ITestComponentMarker, 'hierarchyOutputComponent')
        assertEquals 'hierarchyOutputComponent', ['europe:suisse'], parameterHolder.referencePaths(ITestComponentMarker, 'exampleOutputComponent')
        assertEquals 'reference not found due to wrong marker interface', [], parameterHolder.referencePaths(ITest2ComponentMarker, 'hierarchyOutputComponent')
        assertEquals 'reference not found due to wrong value', [], parameterHolder.referencePaths(ITestComponentMarker, '1.0')
    }

    void testReferencePaths_CBTMDP() {
        ComboBoxTableMultiDimensionalParameter parameter = new ComboBoxTableMultiDimensionalParameter(
                ['exampleOutputComponent', 'hierarchyOutputComponent'], ['component'], ITestComponentMarker
        )
        MultiDimensionalParameterHolder parameterHolder = new MultiDimensionalParameterHolder('europe:suisse', 0, parameter)
        assertEquals 'one reference path found', ['europe:suisse'], parameterHolder.referencePaths(ITestComponentMarker, 'hierarchyOutputComponent')
        assertEquals 'hierarchyOutputComponent', ['europe:suisse'], parameterHolder.referencePaths(ITestComponentMarker, 'exampleOutputComponent')
        assertEquals 'reference not found due to wrong marker interface', [], parameterHolder.referencePaths(ITest2ComponentMarker, 'hierarchyOutputComponent')
        assertEquals 'reference not found due to wrong value', [], parameterHolder.referencePaths(ITestComponentMarker, '1.0')
    }


    void testUpdateReferenceValues_CMDP() {
        IMultiDimensionalConstraints markerConstraint = new ExampleMultiDimensionalConstraints()
        ConstrainedMultiDimensionalParameter parameter = new ConstrainedMultiDimensionalParameter(
                [['exampleOutputComponent', 'hierarchyOutputComponent'], [1.0, 0.0]], ['component', 'values'], markerConstraint
        )
        MultiDimensionalParameterHolder parameterHolder = new MultiDimensionalParameterHolder('europe:suisse', 0, parameter)
        assertFalse(parameterHolder.modified)
        assertEquals 'one reference path found', ['europe:suisse'], parameterHolder.updateReferenceValues(ITestComponentMarker, 'hierarchyOutputComponent', 'flatOutputComponent')
        assertTrue(parameterHolder.modified)
        assertEquals 'correct modification hierarchy -> flat', 'flatOutputComponent', parameterHolder.getBusinessObject().getValueAt(2, 0)
        assertEquals 'one reference path found', ['europe:suisse'], parameterHolder.updateReferenceValues(ITestComponentMarker, 'exampleOutputComponent', 'outputComponent')
        assertEquals 'correct modification example -> flat', 'outputComponent', parameterHolder.getBusinessObject().getValueAt(1, 0)
        assertEquals 'reference not found due to wrong marker interface', [], parameterHolder.updateReferenceValues(ITest2ComponentMarker, 'hierarchyOutputComponent', 'flatOutputComponent')
        assertEquals 'reference not found due to wrong value', [], parameterHolder.updateReferenceValues(ITestComponentMarker, '1.0', '5.0')
    }

    void testUpdateReferenceValues_CBTMDP() {
        ComboBoxTableMultiDimensionalParameter parameter = new ComboBoxTableMultiDimensionalParameter(
                ['exampleOutputComponent', 'hierarchyOutputComponent'], ['component'], ITestComponentMarker
        )
        MultiDimensionalParameterHolder parameterHolder = new MultiDimensionalParameterHolder('europe:suisse', 0, parameter)
        assertFalse(parameterHolder.modified)
        assertEquals 'one reference path found', ['europe:suisse'], parameterHolder.updateReferenceValues(ITestComponentMarker, 'hierarchyOutputComponent', 'flatOutputComponent')
        assertTrue(parameterHolder.modified)
        assertEquals 'correct modification hierarchy -> flat', 'flatOutputComponent', parameterHolder.getBusinessObject().getValueAt(2, 0)
        assertEquals 'one reference path found', ['europe:suisse'], parameterHolder.updateReferenceValues(ITestComponentMarker, 'exampleOutputComponent', 'outputComponent')
        assertEquals 'correct modification example -> flat', 'outputComponent', parameterHolder.getBusinessObject().getValueAt(1, 0)
        assertEquals 'reference not found due to wrong marker interface', [], parameterHolder.updateReferenceValues(ITest2ComponentMarker, 'hierarchyOutputComponent', 'flatOutputComponent')
        assertEquals 'reference not found due to wrong value', [], parameterHolder.updateReferenceValues(ITestComponentMarker, '1.0', '5.0')
    }

    void testClone() {
        ParameterHolder holder = ParameterHolderFactory.getHolder("europe:suisee", 0, new ComboBoxTableMultiDimensionalParameter(
                ['exampleOutputComponent', 'hierarchyOutputComponent'], ['component'], ITestComponentMarker))

        ParameterHolder clone = holder.clone()
        assertFalse clone.added
        assertFalse clone.modified
        assertFalse clone.removed
    }
}
