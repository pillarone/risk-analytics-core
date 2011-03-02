package org.pillarone.riskanalytics.core.fileimport

import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import org.pillarone.riskanalytics.core.output.ResultConfigurationDAO
import org.pillarone.riskanalytics.core.simulation.item.ResultConfiguration

class FileImportServiceIntegrationTests extends GroovyTestCase {

    void testImportModelsIfNeeded() {
        FileImportService.importModelsIfNeeded(["Core"])
        ParameterizationDAO dao = ParameterizationDAO.findByName("CoreParameters")
        assertNotNull dao.model

        Parameterization parameterization = new Parameterization("CoreParameters")
        parameterization.load()

        assertEquals "1", parameterization.modelVersionNumber.toString()

        ResultConfigurationDAO dao2 = ResultConfigurationDAO.findByName("CoreResultConfiguration")
        assertNotNull dao2.model

        ResultConfiguration resultConfiguration = new ResultConfiguration("CoreResultConfiguration")
        resultConfiguration.load()

        assertEquals "1", resultConfiguration.modelVersionNumber.toString()
    }

    void testImportModelsIfNeededMigrated() {
        FileImportService.importModelsIfNeeded(["MigratableCore"])
        ParameterizationDAO dao = ParameterizationDAO.findByName("MigratableCoreParameters")
        assertNotNull dao.model

        Parameterization parameterization = new Parameterization("MigratableCoreParameters")
        parameterization.load()

        assertEquals "2", parameterization.modelVersionNumber.toString()

        ResultConfigurationDAO dao2 = ResultConfigurationDAO.findByName("MigratableCoreResultConfiguration")
        assertNotNull dao2.model

        ResultConfiguration resultConfiguration = new ResultConfiguration("MigratableCoreResultConfiguration")
        resultConfiguration.load()

        assertEquals "2", resultConfiguration.modelVersionNumber.toString()
    }
}
