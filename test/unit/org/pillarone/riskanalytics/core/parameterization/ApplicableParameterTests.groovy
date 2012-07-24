package org.pillarone.riskanalytics.core.parameterization

import org.pillarone.riskanalytics.core.example.component.TestComponent

class ApplicableParameterTests extends GroovyTestCase {

    void testApply() {

        TestComponent component = new TestComponent()
        double originalValue = component.parmValue

        ApplicableParameter parameter = new ApplicableParameter(component: component, parameterPropertyName: "parmValue", parameterValue: 3.0d)

        parameter.apply()

        assertEquals "value not changed", 3.0d, component.parmValue

    }
}