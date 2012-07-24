package org.pillarone.riskanalytics.core.simulation.engine.actions

import models.core.CoreModel
import org.pillarone.riskanalytics.core.ModelStructureDAO
import org.pillarone.riskanalytics.core.output.ConfigObjectHolder
import org.pillarone.riskanalytics.core.simulation.engine.SimulationScope

class PrepareStructureInformationActionTests extends GroovyTestCase {

    void testPerform() {
        CoreModel model = new CoreModel()
        model.init()
        model.injectComponentNames()


        ModelStructureDAO structure = new ModelStructureDAO(name: "test", itemVersion: "1", modelClassName: model.class.name)
        ConfigObjectHolder stringData = new ConfigObjectHolder()
        File structureFile = new File("src/java/models/core/CoreStructure.groovy")
        stringData.data = structureFile.text
        structure.stringData = stringData
        assert stringData.save(), "stringData not saved"
        assert structure.save(), "structure not saved"

        SimulationScope simulationScope = new SimulationScope()
        simulationScope.model = model

        assertNull simulationScope.structureInformation

        new PrepareStructureInformationAction(simulationScope: simulationScope).perform()

        assertNotNull simulationScope.structureInformation
    }
}