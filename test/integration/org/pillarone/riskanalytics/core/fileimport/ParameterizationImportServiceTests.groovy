package org.pillarone.riskanalytics.core.fileimport

import org.pillarone.riskanalytics.core.ParameterizationDAO

class ParameterizationImportServiceTests extends GroovyTestCase {


    private ParameterizationImportService getService() {
        return new ParameterizationImportService()
    }


    void testImportParameterization() {

        File paramFile = new File(getModelFolder(), "core/CoreParameters.groovy")

        def count = ParameterizationDAO.count()

        assertTrue "import not successful", service.importFile(paramFile)
        assertEquals count + 1, ParameterizationDAO.count()

    }


    void testPrepare() {

        File paramFile = new File(getModelFolder(), "core/CoreParameters.groovy")

        ParameterizationImportService parameterizationImportService = getService()
        assertEquals "wrong itemName", "CoreParameters", parameterizationImportService.prepare(paramFile)
        assertNotNull parameterizationImportService.currentConfigObject
        assertEquals "wrong name in config", "CoreParameters", parameterizationImportService.currentConfigObject.displayName

    }

    void testDaoClass() {
        assertEquals "wrong dao class", ParameterizationDAO, service.daoClass
    }

    void testFileSuffix() {
        assertEquals "wrong fileSuffix", "Parameters", service.fileSuffix
    }

    private File getModelFolder() {
        return new File(getClass().getResource("/models").toURI())
    }

}