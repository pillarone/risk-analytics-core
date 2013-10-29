package org.pillarone.riskanalytics.core.fileimport

import org.junit.Test
import org.pillarone.riskanalytics.core.ModelDAO
import models.core.CoreModel
import models.migratableCore.MigratableCoreModel

import static org.junit.Assert.*

public class ModelFileImportServiceTests {

    private ModelFileImportService getService() {
        return new ModelFileImportService()
    }

    @Test
    void testImportFile() {
        File modelFile = new File(getModelFolder(), "core/CoreModel.groovy")
        def count = ModelDAO.count()
        assertTrue "import not successful", service.importFile(modelFile.toURI().toURL())

        assertEquals count + 1, ModelDAO.count()

        ModelDAO dao = ModelDAO.findByName(CoreModel.simpleName)
        assertEquals CoreModel.name, dao.modelClassName
        assertEquals "1", dao.itemVersion

        modelFile = new File(getModelFolder(), "migratableCore/MigratableCoreModel.groovy")
        assertTrue "import not successful", service.importFile(modelFile.toURI().toURL())

        assertEquals count + 2, ModelDAO.count()

        dao = ModelDAO.findByName(MigratableCoreModel.simpleName)
        assertEquals MigratableCoreModel.name, dao.modelClassName
        assertEquals "2", dao.itemVersion

    }

    @Test
    void testPrepare() {

        File modelFile = new File(getModelFolder(), "core/CoreModel.groovy")

        assertEquals "wrong itemName", "CoreModel", service.prepare(modelFile.toURI().toURL(), modelFile.name)

    }


    @Test
    void testDaoClass() {
        assertEquals "wrong dao class", ModelDAO, service.daoClass
    }

    @Test
    void testFileSuffix() {
        assertEquals "wrong fileSuffix", "Model", service.fileSuffix
    }

    @Test
    void testLookupItem() {
        File modelFile = new File(getModelFolder(), "migratableCore/MigratableCoreModel.groovy")
        assertTrue "import not successful", service.importFile(modelFile.toURI().toURL())
        ModelFileImportService importService = getService()

        importService.modelClass = MigratableCoreModel
        importService.fileName = MigratableCoreModel.simpleName
        importService.versionNumber = "2"

        // don't import same file twice
        assertTrue importService.lookUpItem(MigratableCoreModel.simpleName)
        importService.versionNumber = "3"

        // but import same file with new version number
        assertFalse importService.lookUpItem(MigratableCoreModel.simpleName)
    }

    private File getModelFolder() {
        return new File(getClass().getResource("/models").toURI())
    }

}