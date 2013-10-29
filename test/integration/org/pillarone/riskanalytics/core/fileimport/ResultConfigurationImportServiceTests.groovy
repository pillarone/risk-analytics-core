package org.pillarone.riskanalytics.core.fileimport

import org.junit.Test
import org.pillarone.riskanalytics.core.output.ResultConfigurationDAO

import static org.junit.Assert.*

class ResultConfigurationImportServiceTests {

    ResultConfigurationImportService resultConfigurationImportService

    @Test
    void testImportResultConfiguration() {

        File paramFile = new File("src/java/models/core/CoreResultConfiguration.groovy")

        def count = ResultConfigurationDAO.count()

        assertTrue "import not successful", resultConfigurationImportService.importFile(paramFile.toURI().toURL())
        assertEquals count + 1, ResultConfigurationDAO.count()

    }
}