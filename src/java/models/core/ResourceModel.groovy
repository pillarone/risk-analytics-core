package models.core

import org.pillarone.riskanalytics.core.example.component.ExampleComponentContainingResource

class ResourceModel extends CoreModel {

    ExampleComponentContainingResource resourceComponent

    @Override
    void initComponents() {
        super.initComponents()
        resourceComponent = new ExampleComponentContainingResource()
    }


}
