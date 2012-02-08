package models.core

import org.pillarone.riskanalytics.core.simulation.item.VersionNumber

model=models.core.ResourceModel
periodCount=1
components {
    parameterComponent {
        parmValue[0]=2.0
    }
    resourceComponent {
        parmResource[0]=new org.pillarone.riskanalytics.core.components.ResourceHolder(org.pillarone.riskanalytics.core.example.component.ExampleResource,"myResource",new VersionNumber("1"))
    }
}
comments=[]
tags=[]
