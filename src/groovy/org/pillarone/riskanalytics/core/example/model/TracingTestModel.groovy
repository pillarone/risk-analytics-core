package org.pillarone.riskanalytics.core.example.model

import org.pillarone.riskanalytics.core.example.component.TestComponent
import org.pillarone.riskanalytics.core.example.component.TestComposedComponent
import org.pillarone.riskanalytics.core.example.component.TestDynamicComponent
import org.pillarone.riskanalytics.core.model.StochasticModel

class TracingTestModel extends StochasticModel {

    TestComponent underwritingSegments
    TestComponent claimsGenerators
    TestComposedComponent linesOfBusiness
    TestDynamicComponent dynamicCC;

    void initComponents() {
        underwritingSegments = new TestComponent()
        claimsGenerators = new TestComponent()
        linesOfBusiness = new TestComposedComponent()
        dynamicCC = new TestDynamicComponent();

        addStartComponent underwritingSegments
        addStartComponent claimsGenerators
    }

    void wireComponents() {
        claimsGenerators.input1 = underwritingSegments.outValue1
        dynamicCC.input1 = claimsGenerators.outValue1
        linesOfBusiness.input1 = dynamicCC.outValue1
        linesOfBusiness.input1 = underwritingSegments.outValue1

    }
}

