package org.pillarone.riskanalytics.core.fileimport

import org.pillarone.riskanalytics.core.ModelDAO

public class ModelFileImportServiceTests extends GroovyTestCase {

    private ModelFileImportService getService() {
        return new ModelFileImportService()
    }

    void testImportFile() {

        File modelFile = new File(getModelFolder(), "core/CoreModel.groovy")
        def count = ModelDAO.count()
        assertTrue "import not successful", service.importFile(modelFile)

        assertEquals count + 1, ModelDAO.count()

    }

    void testPrepare() {

        File modelFile = new File(getModelFolder(), "core/CoreModel.groovy")

        assertEquals "wrong itemName", "CoreModel", service.prepare(modelFile)

    }


    void testDaoClass() {
        assertEquals "wrong dao class", ModelDAO, service.daoClass
    }

    void testFileSuffix() {
        assertEquals "wrong fileSuffix", "Model", service.fileSuffix
    }

    private File getModelFolder() {
        return new File(getClass().getResource("/models").toURI())
    }

}