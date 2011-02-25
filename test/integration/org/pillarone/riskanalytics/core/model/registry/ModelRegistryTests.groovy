package org.pillarone.riskanalytics.core.model.registry

import org.pillarone.riskanalytics.core.fileimport.FileImportService
import org.pillarone.riskanalytics.core.example.model.EmptyModel
import org.pillarone.riskanalytics.core.ModelDAO
import org.pillarone.riskanalytics.core.ModelStructureDAO


class ModelRegistryTests extends GroovyTestCase {


    void testReadFromDatabase() {
        FileImportService.importModelsIfNeeded(["Core"])
        ModelRegistry registry = ModelRegistry.instance
        assertEquals 1, registry.allModelClasses.size()

        assertNull ModelDAO.findByName(EmptyModel.simpleName)
        assertNull ModelStructureDAO.findByModelClassName(EmptyModel.name)
        registry.addModel(EmptyModel)
        assertEquals 2, registry.allModelClasses.size()
        assertNotNull ModelDAO.findByName(EmptyModel.simpleName)
        assertNotNull ModelStructureDAO.findByModelClassName(EmptyModel.name)
    }


}
