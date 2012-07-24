package org.pillarone.riskanalytics.core.fileimport

import org.pillarone.riskanalytics.core.ParameterizationDAO

class FileImportServiceTests extends GroovyTestCase {

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

        filesToImport.each {URL file ->
            assertTrue "wrong file selected ${file.toExternalForm()}", file.toExternalForm().endsWith("Parameters.groovy")
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

        filesToImport.each {URL file ->
            assertTrue "wrong file selected ${file.getFile()}", file.getFile().contains("Core")
            assertTrue "wrong file selected ${file.getFile()}", file.getFile().endsWith("Parameters.groovy")
        }
    }

    void testImportFile() {

        FileImportService fileImportService = [
                getFileSuffix: {return "Parameters"},
                getDaoClass: {return ParameterizationDAO},
                lookUpItem: {Class c, String name -> return false},
                saveItemObject: {fileContent -> true},
                getModelClassName: {return "models.core.CoreModel"},
                prepare: { file, name -> return "notYetImported"}
        ] as FileImportService

        assertTrue fileImportService.importFile(new File(getModelFolder(), "/core/CoreParameters.groovy").toURI().toURL())

    }

    void testImportFileWhenAlreadyExists() {

        FileImportService fileImportService = [
                getFileSuffix: {return "Parameters"},
                getDaoClass: {return ParameterizationDAO},
                lookUpItem: {Class c, String name -> return true},
                saveItemObject: {fileContent -> true},
                getModelClassName: {return "models.core.CoreModel"},
                prepare: {file, name -> return "notYetImported"}
        ] as FileImportService

        assertFalse fileImportService.importFile(new File(getModelFolder(), "/core/CoreParameters.groovy").toURI().toURL())

    }

    void testImportFileWhenSaveFails() {

        FileImportService fileImportService = [
                getFileSuffix: {return "Parameters"},
                getDaoClass: {return ParameterizationDAO},
                lookUpItem: {Class c, String name -> return true},
                saveItemObject: {fileContent -> false},
                getModelClassName: {return "models.core.CoreModel"},
                prepare: {file, name -> return "notYetImported"}
        ] as FileImportService

        assertFalse fileImportService.importFile(new File(getModelFolder(), "/core/CoreParameters.groovy").toURI().toURL())

    }

    void testShouldImportModel() {
        FileImportService fileImportService = [
                getFileSuffix: {return "Parameters"},
                getDaoClass: {return null},
                saveItemObject: {itemObject ->},
                prepare: {file, name -> }
        ] as FileImportService

        assertTrue "no model specified", fileImportService.shouldImportModel("CoreParameters.groovy", null)
        assertTrue "no model specified", fileImportService.shouldImportModel("CoreParameters.groovy", [])
        assertTrue "matching model specified", fileImportService.shouldImportModel("CoreParameters.groovy", ["Core"])
        assertFalse "non-matching model specified", fileImportService.shouldImportModel("CoreParameters.groovy", ["CapitalEagle"])

    }
}