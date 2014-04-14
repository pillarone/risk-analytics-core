package org.pillarone.riskanalytics.core.parameterization

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import models.core.CoreModel
import org.pillarone.riskanalytics.core.simulation.item.ModelStructure

@TestMixin(GrailsUnitTestMixin)
class StructureInformationInjectorTests {

    void testWithModelStructureItem() {
        ModelStructure modelStructure = new ModelStructure("CoreStructure")
        ConfigObject config = new ConfigObject()
        config.model = CoreModel
        config.periodCount = 1
        config.company.hierarchyLevel.components.hierarchyOutputComponent = new ConfigObject()
        modelStructure.data = config

        CoreModel model = new CoreModel()
        model.initComponents()
        StructureInformationInjector injector = new StructureInformationInjector(modelStructure, model)
        ConfigObject structure = injector.configObject

        assertSame structure.company.hierarchyLevel.components.hierarchyOutputComponent, model.hierarchyOutputComponent

    }

    void testLoadStructure() {
        StructureInformationInjector injector = new StructureInformationInjector("src/java/models/core/CoreStructure", new CoreModel())
        assertNotNull injector.configObject
    }

    void testLoadUnknownStructure() {
        StructureInformationInjector injector = new StructureInformationInjector("unknownFileName", new CoreModel())
        assertNotNull(injector.configObject)
        assertTrue(injector.configObject.isEmpty())
    }

    void testStructureBuilding() {
        CoreModel model = new CoreModel()
        model.initComponents()
        StructureInformationInjector injector = new StructureInformationInjector("src/java/models/core/CoreStructure", model)
        ConfigObject structure = injector.configObject

        assertSame structure.company.hierarchyLevel.components.hierarchyOutputComponent, model.hierarchyOutputComponent
    }
}
