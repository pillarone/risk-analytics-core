package org.pillarone.riskanalytics.core.fileimport

import grails.test.GrailsUnitTestCase
import org.pillarone.riskanalytics.core.ParameterizationDAO

class FileImportServiceTests extends GrailsUnitTestCase {

    private File getModelFolder() {
        return new File(getClass().getResource("/models").toURI())
    }

    void testScanImportFolder() {

        FileImportService fileImportService = [
                getFileSuffix: {return "Parameters"},
                getDaoClass: {return null},
                saveItemObject: {itemObject ->},
                prepare: {File file ->}
        ] as FileImportService

        List filesToImport = fileImportService.scanImportFolder()

        assertNotNull filesToImport
        assertFalse filesToImport.empty

        filesToImport.each {File file ->
            assertTrue "wrong file selected ${file.name}", file.name.endsWith("Parameters.groovy")
        }
    }

    void testScanImportFolderWithModel() {

        FileImportService fileImportService = [
                getFileSuffix: {return "Parameters"},
                getDaoClass: {return null},
                saveItemObject: {itemObject ->},
                prepare: {File file -> }
        ] as FileImportService

        List filesToImport = fileImportService.scanImportFolder(["Core"])

        assertNotNull filesToImport
        assertFalse filesToImport.empty

        filesToImport.each {File file ->
            assertTrue "wrong file selected ${file.name}", file.name.startsWith("Core")
            assertTrue "wrong file selected ${file.name}", file.name.endsWith("Parameters.groovy")
        }
    }

    void testImportFile() {

        FileImportService fileImportService = [
                getFileSuffix: {return "Parameters"},
                getDaoClass: {return ParameterizationDAO},
                lookUpItem: {Class c, String name -> return false},
                saveItemObject: {fileContent -> true},
                getModelClassName: {return "models.core.CoreModel"},
                prepare: {File file -> return "notYetImported"}
        ] as FileImportService

        assertTrue fileImportService.importFile(new File(getModelFolder(), "/core/CoreParameters.groovy"))

    }

    void testImportFileWhenAlreadyExists() {

        FileImportService fileImportService = [
                getFileSuffix: {return "Parameters"},
                getDaoClass: {return ParameterizationDAO},
                lookUpItem: {Class c, String name -> return true},
                saveItemObject: {fileContent -> true},
                getModelClassName: {return "models.core.CoreModel"},
                prepare: {File file -> return "notYetImported"}
        ] as FileImportService

        assertFalse fileImportService.importFile(new File(getModelFolder(), "/core/CoreParameters.groovy"))

    }

    void testImportFileWhenSaveFails() {

        FileImportService fileImportService = [
                getFileSuffix: {return "Parameters"},
                getDaoClass: {return ParameterizationDAO},
                lookUpItem: {Class c, String name -> return true},
                saveItemObject: {fileContent -> false},
                getModelClassName: {return "models.core.CoreModel"},
                prepare: {File file -> return "notYetImported"}
        ] as FileImportService

        assertFalse fileImportService.importFile(new File(getModelFolder(), "/core/CoreParameters.groovy"))

    }

    void testShouldImportModel() {
        FileImportService fileImportService = [
                getFileSuffix: {return "Parameters"},
                getDaoClass: {return null},
                saveItemObject: {itemObject ->},
                prepare: {File file -> }
        ] as FileImportService

        assertTrue "no model specified", fileImportService.shouldImportModel("CoreParameters.groovy", null)
        assertTrue "no model specified", fileImportService.shouldImportModel("CoreParameters.groovy", [])
        assertTrue "matching model specified", fileImportService.shouldImportModel("CoreParameters.groovy", ["Core"])
        assertFalse "non-matching model specified", fileImportService.shouldImportModel("CoreParameters.groovy", ["CapitalEagle"])

    }
}