package org.pillarone.riskanalytics.core.simulation.item

import org.junit.Test
import org.pillarone.riskanalytics.core.ModelStructureDAO
import org.pillarone.riskanalytics.core.output.ConfigObjectHolder

import static org.junit.Assert.*

class ModelStructureTests {


    @Test
    void testFindAllModelClasses() {
        ModelStructureDAO.withNewSession { def session ->
            ModelStructureDAO.list()*.delete()
            session.flush()
        }
        ModelStructureDAO exampleCompanyStructure = new ModelStructureDAO()
        exampleCompanyStructure.name = "ExampleCompany"
        exampleCompanyStructure.modelClassName = "ExampleCompanyModel"
        ConfigObjectHolder holder = new ConfigObjectHolder(data: '')
        holder.save()
        exampleCompanyStructure.stringData = holder
        exampleCompanyStructure.save()

        ModelStructureDAO sparrowStructure = new ModelStructureDAO()
        sparrowStructure.name = "Sparrow"
        sparrowStructure.modelClassName = "SparrowModel"
        holder = new ConfigObjectHolder(data: '')
        holder.save()
        sparrowStructure.stringData = holder
        sparrowStructure.save()

        List modeClassNamesFromDB = ModelStructureDAO.list().modelClassName

        List modelClasses = ModelStructure.findAllModelClasses()

        assertEquals "not all model classes rerieved", modeClassNamesFromDB.size(), modelClasses.size()
        modelClasses.each { Class modelClass ->
            assertTrue "modelClass not in DB: ${modelClass.name}", modeClassNamesFromDB.contains(modelClass.name)
            modeClassNamesFromDB.remove(modelClass.name)
        }
        assertTrue "modelClasses not retrieved: ${modeClassNamesFromDB}", modeClassNamesFromDB.empty
    }
}