package models.resource

import org.pillarone.riskanalytics.core.simulation.item.VersionNumber
import org.pillarone.riskanalytics.core.example.parameter.ExampleParameterObjectClassifier
import org.pillarone.riskanalytics.core.parameterization.ConstrainedMultiDimensionalParameter
import org.pillarone.riskanalytics.core.components.ResourceHolder
import org.pillarone.riskanalytics.core.example.component.ExampleResource
import org.pillarone.riskanalytics.core.parameterization.ConstraintsFactory
import org.pillarone.riskanalytics.core.example.parameter.ExampleResourceConstraints

model = ResourceModel
periodCount = 1
components {
    parameterComponent {
        parmValue[0] = 2.0
    }
    resourceComponent {
        parmResource[0] = new org.pillarone.riskanalytics.core.components.ResourceHolder(org.pillarone.riskanalytics.core.example.component.ExampleResource, "myResource", new VersionNumber("1"))
        parmParameterObject[0] = ExampleParameterObjectClassifier.RESOURCE.getParameterObject(["resource": new ConstrainedMultiDimensionalParameter([[new ResourceHolder(ExampleResource, "myResource", new VersionNumber("1"))]], ['title'], ConstraintsFactory.getConstraints(ExampleResourceConstraints.IDENTIFIER))])
    }
}
comments = []
tags = []
