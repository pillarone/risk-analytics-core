package org.pillarone.riskanalytics.core.fileimport

import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import org.pillarone.riskanalytics.core.output.ResultConfigurationDAO
import org.pillarone.riskanalytics.core.simulation.item.ResultConfiguration
import org.pillarone.riskanalytics.core.ModelStructureDAO
import org.pillarone.riskanalytics.core.model.registry.ModelRegistry
import models.migratableCore.MigratableCoreModel
import models.core.CoreModel

class FileImportServiceIntegrationTests extends GroovyTestCase {

    void setUp() {
        ModelRegistry.instance.clear()
    }

    @Override
    protected void tearDown() {
        System.setProperty("skipImport", "false")
        super.tearDown()
    }

    void testImportModelsIfNeeded() {
        FileImportService.importModelsIfNeeded(["Core"])
        ParameterizationDAO dao = ParameterizationDAO.findByName("CoreParameters")
        assertNotNull dao.model

        Parameterization parameterization = new Parameterization("CoreParameters")
        parameterization.modelClass = CoreModel
        parameterization.load()

        assertEquals "1", parameterization.modelVersionNumber.toString()

        ResultConfigurationDAO dao2 = ResultConfigurationDAO.findByName("CoreResultConfiguration")
        assertNotNull dao2.model

        ResultConfiguration resultConfiguration = new ResultConfiguration("CoreResultConfiguration")
        resultConfiguration.modelClass = CoreModel
        resultConfiguration.load()

        assertEquals "1", resultConfiguration.modelVersionNumber.toString()
    }

    void testImportModelsIfNeededMigrated() {
        FileImportService.importModelsIfNeeded(["MigratableCore"])
        ParameterizationDAO dao = ParameterizationDAO.findByName("MigratableCoreParameters")
        assertNotNull dao.model

        Parameterization parameterization = new Parameterization("MigratableCoreParameters")
        parameterization.modelClass = MigratableCoreModel
        parameterization.load()

        assertEquals "2", parameterization.modelVersionNumber.toString()

        ResultConfigurationDAO dao2 = ResultConfigurationDAO.findByName("MigratableCoreResultConfiguration")
        assertNotNull dao2.model

        ResultConfiguration resultConfiguration = new ResultConfiguration("MigratableCoreResultConfiguration")
        resultConfiguration.modelClass = MigratableCoreModel
        resultConfiguration.load()

        assertEquals "2", resultConfiguration.modelVersionNumber.toString()
    }

    void testSkipImportTrue() {
        System.setProperty("skipImport", "true")
        FileImportService.importModelsIfNeeded(["Core"])

        assertEquals 0, ParameterizationDAO.count()

        assertEquals ModelStructureDAO.count() as int, ModelRegistry.instance.allModelClasses.size()
    }

    void testSkipImportTrueWithExisting() {

        new ModelStructureImportService().compareFilesAndWriteToDB(['Core'])
        new ModelFileImportService().compareFilesAndWriteToDB(['Core'])
        new ParameterizationImportService().compareFilesAndWriteToDB(['Core'])
        new ResultConfigurationImportService().compareFilesAndWriteToDB(['Core'])

        assertEquals 0, ModelRegistry.instance.allModelClasses.size()
        long currentCount = ModelStructureDAO.count()
        assertTrue currentCount > 0


        System.setProperty("skipImport", "true")
        FileImportService.importModelsIfNeeded(["Core"])

        assertEquals currentCount, ModelStructureDAO.count()

        assertEquals ModelStructureDAO.count() as int, ModelRegistry.instance.allModelClasses.size()
    }

    void testSkipImportFalse() {
        System.setProperty("skipImport", "false")
        FileImportService.importModelsIfNeeded(["Core"])

        assertTrue ParameterizationDAO.count() > 0

        assertEquals ModelStructureDAO.count() as int, ModelRegistry.instance.allModelClasses.size()
    }
}
