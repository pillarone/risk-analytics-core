package org.pillarone.riskanalytics.core.parameterization

import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import org.pillarone.riskanalytics.core.model.Model
import models.core.CoreModel
import org.pillarone.riskanalytics.core.example.marker.ITestComponentMarker
import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.example.parameter.ExampleMultiDimensionalConstraints
import org.pillarone.riskanalytics.core.example.marker.ITest2ComponentMarker


class ConstrainedMultiDimensionalParameterTests extends GroovyTestCase {

    private Parameterization getParameterization(File paramFile) {
        ConfigObject params = new ConfigSlurper().parse(paramFile.toURL())
        return ParameterizationHelper.createParameterizationFromConfigObject(params, paramFile.name)
    }

    void testGetValuesAsObjects() {
        Parameterization parameterization = getParameterization(new File("src/java/models/core/CoreParameters.groovy"))
        Model model = new CoreModel()
        model.init()
        model.injectComponentNames()

        ParameterApplicator applicator = new ParameterApplicator(parameterization: parameterization, model: model)
        applicator.init()
        applicator.applyParameterForPeriod(0)

        IMultiDimensionalConstraints markerConstraint = [
                getColumnType: { int index -> return index == 0 ? ITestComponentMarker : BigDecimal }
        ] as IMultiDimensionalConstraints

        ConstrainedMultiDimensionalParameter constrainedMultiDimensionalParameter = new ConstrainedMultiDimensionalParameter([['exampleOutputComponent', 'hierarchyOutputComponent'], [1.0, 0.0]], ['component', 'values'], markerConstraint)
        constrainedMultiDimensionalParameter.simulationModel = model
        List components = constrainedMultiDimensionalParameter.getValuesAsObjects(0)
        assertEquals 2, components.size()
        assertTrue components.every { it instanceof Component }

        List numbers = constrainedMultiDimensionalParameter.getValuesAsObjects(1)
        assertEquals 2, numbers.size()
        assertTrue numbers.every { it instanceof Number }
    }


    void testReferencePaths() {
        IMultiDimensionalConstraints markerConstraint = new ExampleMultiDimensionalConstraints()
        ConstrainedMultiDimensionalParameter parameter = new ConstrainedMultiDimensionalParameter(
                [['exampleOutputComponent', 'hierarchyOutputComponent'], [1.0, 0.0]], ['component', 'values'], markerConstraint
        )
        assertTrue 'hierarchyOutputComponent', parameter.referencePaths(ITestComponentMarker, 'hierarchyOutputComponent')
        assertTrue 'hierarchyOutputComponent', parameter.referencePaths(ITestComponentMarker, 'exampleOutputComponent')
        assertFalse 'hierarchyOutputComponent', parameter.referencePaths(ITest2ComponentMarker, 'hierarchyOutputComponent')
        assertFalse 'hierarchyOutputComponent', parameter.referencePaths(ITestComponentMarker, '1.0')
    }


    void testUpdateReferenceValues() {
        IMultiDimensionalConstraints markerConstraint = new ExampleMultiDimensionalConstraints()
        ConstrainedMultiDimensionalParameter parameter = new ConstrainedMultiDimensionalParameter(
                [['exampleOutputComponent', 'hierarchyOutputComponent'], [1.0, 0.0]], ['component', 'values'], markerConstraint
        )
        assertTrue 'hierarchyOutputComponent', parameter.updateReferenceValues(ITestComponentMarker, 'hierarchyOutputComponent', 'flatOutputComponent')
        assertTrue 'hierarchyOutputComponent', parameter.updateReferenceValues(ITestComponentMarker, 'exampleOutputComponent', 'flatOutputComponent')
        assertFalse 'hierarchyOutputComponent', parameter.updateReferenceValues(ITest2ComponentMarker, 'hierarchyOutputComponent', 'flatOutputComponent')
        assertFalse 'hierarchyOutputComponent', parameter.updateReferenceValues(ITestComponentMarker, '1.0', '5.0')
    }
}
