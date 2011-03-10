package models.migratableCore

import models.core.CoreModel
import org.pillarone.riskanalytics.core.model.MigratableModel
import org.pillarone.riskanalytics.core.simulation.item.VersionNumber
import org.pillarone.riskanalytics.core.model.migration.AbstractMigration
import org.pillarone.riskanalytics.core.model.migration.MigrationUtils


class MigratableCoreModel extends CoreModel implements MigratableModel {

    VersionNumber getVersion() {
        return new VersionNumber("2")
    }

    List<AbstractMigration> getMigrationChain(VersionNumber from, VersionNumber to) {
        MigrationUtils.getMigrationChain([], from, to)
    }


}
