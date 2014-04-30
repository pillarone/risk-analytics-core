package models.dependingCore

import models.core.CoreModel
import org.pillarone.riskanalytics.core.example.component.DependingComponent


class DependingCoreModel extends CoreModel {

    DependingComponent dependingComponent

    @Override
    void initComponents() {
        dependingComponent = new DependingComponent()
        super.initComponents()
    }
}
