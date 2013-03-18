package org.pillarone.riskanalytics.core.model.migration

import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.simulation.item.VersionNumber
import models.migratableCore.MigratableCoreModel
import org.pillarone.riskanalytics.core.fileimport.FileImportService

import static org.junit.Assert.*
import org.pillarone.riskanalytics.core.parameterization.IParameterObjectClassifier
import org.pillarone.riskanalytics.core.example.parameter.ExampleParameterObjectClassifier
import org.pillarone.riskanalytics.core.parameterization.AbstractParameterObjectClassifier
import org.pillarone.riskanalytics.core.example.migration.TestConstrainedTable
import org.pillarone.riskanalytics.core.parameterization.AbstractMultiDimensionalParameter


class MigrationSupportTests extends GroovyTestCase {

    @Override
    protected void setUp() {
        FileImportService.importModelsIfNeeded(['MigratableCoreModel'])
    }

    void testClassifiers() {
        MigrationSupportImpl support = new MigrationSupportImpl(new VersionNumber("1"), new VersionNumber("2"), MigratableCoreModel)
        Model newModel = new MigratableCoreModel()
        Model oldModel = new ModelMigrationClassLoader([support.getOldModelJarURL()] as URL[], Thread.currentThread().contextClassLoader).loadClass(MigratableCoreModel.name).newInstance()

        oldModel.init()
        newModel.init()

        support.migrateParameterization(oldModel, newModel)
    }
}

class MigrationSupportImpl extends MigrationSupport {

    MigrationSupportImpl(VersionNumber from, VersionNumber to, Class modelClass) {
        super(from, to, modelClass)
    }

    @Override
    void doMigrateParameterization(Model source, Model target) {
//        source = source as MigratableCoreModel

        // *** test getNewClassifier
        AbstractParameterObjectClassifier type = source.exampleInputOutputComponent.parmParameterObject.getType()
        assertEquals ExampleParameterObjectClassifier.name, type.class.name
        assertFalse type instanceof ExampleParameterObjectClassifier

        IParameterObjectClassifier newClassifier = getNewClassifier(type)
        assertEquals ExampleParameterObjectClassifier.name, newClassifier.class.name
        assertTrue newClassifier instanceof ExampleParameterObjectClassifier

        assertSame newClassifier.getClass()."${type.typeName}", newClassifier
        //***


        //*** test if removeColumnFromConstraint works for empty mdps
        ConstrainedMultiDimensionalParameterCollector collector = new ConstrainedMultiDimensionalParameterCollector(TestConstrainedTable.newInstance())
        target.accept(collector)
        AbstractMultiDimensionalParameter mdp = collector.result[0]
        mdp.values = [[]]
        assertEquals 0, mdp.valueRowCount

        removeColumnFromConstraint(TestConstrainedTable, 1)
    }

}
