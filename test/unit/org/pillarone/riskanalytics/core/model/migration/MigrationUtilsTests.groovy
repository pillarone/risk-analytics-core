package org.pillarone.riskanalytics.core.model.migration

import org.pillarone.riskanalytics.core.simulation.item.VersionNumber
import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.example.model.EmptyModel


class MigrationUtilsTests extends GroovyTestCase {

    void testSimple() {
        TestMigration migration = new TestMigration("1", "2")

        List<AbstractMigration> chain = MigrationUtils.getMigrationChain([migration], new VersionNumber("1"), new VersionNumber("2"))
        assertEquals 1, chain.size()
        assertSame migration, chain[0]
    }

    void testThreeNodes() {
        TestMigration migration = new TestMigration("1", "2")
        TestMigration migration2 = new TestMigration("2", "3")

        List<AbstractMigration> chain = MigrationUtils.getMigrationChain([migration, migration2], new VersionNumber("1"), new VersionNumber("3"))
        assertEquals 2, chain.size()
        assertSame migration, chain[0]
        assertSame migration2, chain[1]
    }

    void testThreeNodesDirect() {
        TestMigration migration = new TestMigration("1", "2")
        TestMigration migration2 = new TestMigration("2", "3")
        TestMigration migration3 = new TestMigration("1", "3")

        List<AbstractMigration> chain = MigrationUtils.getMigrationChain([migration, migration2, migration3], new VersionNumber("1"), new VersionNumber("3"))
        assertEquals 1, chain.size()
        assertSame migration3, chain[0]
    }


}


class TestMigration extends AbstractMigration {

    TestMigration(String from, String to) {
        super(new VersionNumber(from), new VersionNumber(to), EmptyModel)
    }

    @Override
    void migrateParameterization(Model source, Model target) {

    }

}
