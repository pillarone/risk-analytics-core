package org.pillarone.riskanalytics.core.simulation.engine.actions

import grails.test.GrailsUnitTestCase
import models.core.CoreModel
import org.pillarone.riskanalytics.core.ModelStructureDAO
import org.pillarone.riskanalytics.core.output.ConfigObjectHolder
import org.pillarone.riskanalytics.core.simulation.engine.SimulationScope
import umontreal.iro.lecuyer.simevents.Sim
import org.pillarone.riskanalytics.core.simulation.item.Simulation
import org.pillarone.riskanalytics.core.fileimport.ModelStructureImportService
import org.pillarone.riskanalytics.core.simulation.item.ModelStructure

class PrepareStructureInformationActionTests extends GrailsUnitTestCase {

    void testPerform() {

        new ModelStructureImportService().compareFilesAndWriteToDB(['CoreStructure'])

        CoreModel model = new CoreModel()
        model.init()
        model.injectComponentNames()

        SimulationScope simulationScope = new SimulationScope()
        simulationScope.model = model
        Simulation simulation = new Simulation("name")
        ModelStructure structure = ModelStructure.getStructureForModel(model.class)
        structure.load()
        simulation.structure = structure
        simulationScope.simulation = simulation

        assertNull simulationScope.structureInformation

        new PrepareStructureInformationAction(simulationScope: simulationScope).perform()

        assertNotNull simulationScope.structureInformation
    }
}