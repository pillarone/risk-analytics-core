package org.pillarone.riskanalytics.core.model.migration

import models.migratableCore.MigratableCoreModel
import org.pillarone.riskanalytics.core.example.parameter.ExampleParameterObjectClassifier
import org.pillarone.riskanalytics.core.fileimport.ModelFileImportService
import org.pillarone.riskanalytics.core.fileimport.ModelStructureImportService
import org.pillarone.riskanalytics.core.fileimport.ResultConfigurationImportService
import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolderFactory

class ModelMigratorTests extends GroovyTestCase {

    void setUp() {
        new ModelFileImportService().compareFilesAndWriteToDB(["MigratableCore"])
        new ModelStructureImportService().compareFilesAndWriteToDB(["MigratableCore"])
        new ResultConfigurationImportService().compareFilesAndWriteToDB(["MigratableCore"])

        //fake an old parameterization where parmNewParameterObject did not yet exist
        Parameterization old = new Parameterization("MigratableCoreParams")
        old.periodCount = 1
        old.modelClass = MigratableCoreModel
        old.addParameter(ParameterHolderFactory.getHolder("dynamicComponent:subSubcomponent:parmParameterObject", 0, ExampleParameterObjectClassifier.TYPE0.getParameterObject(["a": 0d, "b": 1d])))
        old.addParameter(ParameterHolderFactory.getHolder("exampleInputOutputComponent:parmParameterObject", 0, ExampleParameterObjectClassifier.TYPE0.getParameterObject(["a": 0d, "b": 1d])))

        old.save()
    }

    void testOldModel() {
        ModelMigrator migrator = new ModelMigrator(MigratableCoreModel)
        migrator.migrateParameterizations()

        Parameterization old = new Parameterization("MigratableCoreParams")
        old.modelClass = MigratableCoreModel
        old.load()

        assertEquals 6, old.parameterHolders.size()
    }
}
