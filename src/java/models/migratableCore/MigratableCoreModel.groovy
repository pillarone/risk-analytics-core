package models.migratableCore

import models.core.CoreModel
import org.pillarone.riskanalytics.core.model.MigratableModel
import org.pillarone.riskanalytics.core.simulation.item.VersionNumber
import org.pillarone.riskanalytics.core.model.migration.AbstractMigration
import org.pillarone.riskanalytics.core.model.migration.MigrationUtils
import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.example.parameter.ExampleParameterObjectClassifier
import org.pillarone.riskanalytics.core.example.migration.RemoveParamMigrationComponent
import org.pillarone.riskanalytics.core.example.migration.DynamicMigrationComponent


class MigratableCoreModel extends CoreModel implements MigratableModel {

    RemoveParamMigrationComponent composite
    DynamicMigrationComponent dynamic

    @Override
    void initComponents() {
        super.initComponents()
        composite = new RemoveParamMigrationComponent()
        dynamic = new DynamicMigrationComponent()
    }

    VersionNumber getVersion() {
        return new VersionNumber("2")
    }

    List<AbstractMigration> getMigrationChain(VersionNumber from, VersionNumber to) {
        MigrationUtils.getMigrationChain([new Migration_v1_v2()], from, to)
    }

    private static class Migration_v1_v2 extends AbstractMigration {

        Migration_v1_v2() {
            super(new VersionNumber("1"), new VersionNumber("2"), MigratableCoreModel)
        }

        @Override
        void migrateParameterization(Model source, Model target) {
        }

    }

}
