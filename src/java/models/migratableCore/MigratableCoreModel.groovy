package models.migratableCore

import models.core.CoreModel
import org.pillarone.riskanalytics.core.model.MigratableModel
import org.pillarone.riskanalytics.core.simulation.item.VersionNumber


class MigratableCoreModel extends CoreModel implements MigratableModel {

    VersionNumber getVersion() {
        return new VersionNumber("2")
    }


}
