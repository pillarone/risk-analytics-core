package org.pillarone.riskanalytics.core.fileimport

import org.pillarone.riskanalytics.core.ModelStructureDAO

class ModelStructureImportServiceTests extends GroovyTestCase {


    private FileImportService getService() {
        return new ModelStructureImportService()
    }

    void testImportFile() {

        File modelFile = new File(getModelFolder(), "core/CoreStructure.groovy")
        def count = ModelStructureDAO.count()
        assertTrue "import not successful", service.importFile(modelFile)

        assertEquals count + 1, ModelStructureDAO.count()

    }

    void testPrepare() {

        File modelFile = new File(getModelFolder(), "core/CoreStructure.groovy")
        FileImportService modelStructureService = getService()
        assertEquals "wrong itemName", "CoreStructure", modelStructureService.prepare(modelFile)
        assertNotNull modelStructureService.currentConfigObject
        assertEquals "wrong name in config", "CoreStructure", modelStructureService.currentConfigObject.displayName


    }


    void testDaoClass() {
        assertEquals "wrong dao class", ModelStructureDAO, service.daoClass
    }

    void testFileSuffix() {
        assertEquals "wrong fileSuffix", "Structure", service.fileSuffix
    }

    private File getModelFolder() {
        return new File(getClass().getResource("/models").toURI())
    }

}