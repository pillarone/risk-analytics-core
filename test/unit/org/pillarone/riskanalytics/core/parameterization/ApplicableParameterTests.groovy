package org.pillarone.riskanalytics.core.parameterization

import grails.test.GrailsUnitTestCase

import org.pillarone.riskanalytics.core.example.component.TestComponent

class ApplicableParameterTests extends GrailsUnitTestCase {

    void testApply() {

        TestComponent component = new TestComponent()
        double originalValue = component.parmValue

        ApplicableParameter parameter = new ApplicableParameter(component: component, parameterPropertyName: "parmValue", parameterValue: 3.0d)

        parameter.apply()

        assertEquals "value not changed", 3.0d, component.parmValue

    }
}