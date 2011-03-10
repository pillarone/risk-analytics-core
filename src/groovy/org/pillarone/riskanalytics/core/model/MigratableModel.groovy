package org.pillarone.riskanalytics.core.model

import org.pillarone.riskanalytics.core.simulation.item.VersionNumber
import org.pillarone.riskanalytics.core.model.migration.AbstractMigration


public interface MigratableModel {

    VersionNumber getVersion()

    List<AbstractMigration> getMigrationChain(VersionNumber from, VersionNumber to)

}