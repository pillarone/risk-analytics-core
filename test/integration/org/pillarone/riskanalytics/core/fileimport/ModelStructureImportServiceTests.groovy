package org.pillarone.riskanalytics.core.fileimport

import org.junit.Test
import org.pillarone.riskanalytics.core.ModelStructureDAO

import static org.junit.Assert.*

class ModelStructureImportServiceTests {


    private FileImportService getService() {
        return new ModelStructureImportService()
    }

    @Test
    void testImportFile() {

        File modelFile = new File(getModelFolder(), "core/CoreStructure.groovy")
        def count = ModelStructureDAO.count()
        assertTrue "import not successful", service.importFile(modelFile.toURI().toURL())

        assertEquals count + 1, ModelStructureDAO.count()

    }

    @Test
    void testPrepare() {

        File modelFile = new File(getModelFolder(), "core/CoreStructure.groovy")
        FileImportService modelStructureService = getService()
        assertEquals "wrong itemName", "CoreStructure", modelStructureService.prepare(modelFile.toURI().toURL(), modelFile.name)
        assertNotNull modelStructureService.currentConfigObject
        assertEquals "wrong name in config", "CoreStructure", modelStructureService.currentConfigObject.displayName


    }


    @Test
    void testDaoClass() {
        assertEquals "wrong dao class", ModelStructureDAO, service.daoClass
    }

    @Test
    void testFileSuffix() {
        assertEquals "wrong fileSuffix", "Structure", service.fileSuffix
    }

    private File getModelFolder() {
        return new File(getClass().getResource("/models").toURI())
    }

}