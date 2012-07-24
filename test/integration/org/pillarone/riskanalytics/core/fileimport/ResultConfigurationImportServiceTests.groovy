package org.pillarone.riskanalytics.core.fileimport

import org.pillarone.riskanalytics.core.output.ResultConfigurationDAO

class ResultConfigurationImportServiceTests extends GroovyTestCase {

    ResultConfigurationImportService resultConfigurationImportService

    void testImportResultConfiguration() {

        File paramFile = new File("src/java/models/core/CoreResultConfiguration.groovy")

        def count = ResultConfigurationDAO.count()

        assertTrue "import not successful", resultConfigurationImportService.importFile(paramFile.toURI().toURL())
        assertEquals count + 1, ResultConfigurationDAO.count()

    }
}