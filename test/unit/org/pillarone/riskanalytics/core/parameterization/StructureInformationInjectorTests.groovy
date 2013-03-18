package org.pillarone.riskanalytics.core.parameterization

import groovy.mock.interceptor.StubFor
import models.core.CoreModel
import org.pillarone.riskanalytics.core.simulation.item.ModelStructure

class StructureInformationInjectorTests extends GroovyTestCase {

    StubFor structureStub

    protected void setUp() {
        super.setUp()
        structureStub = new StubFor(ModelStructure)
        structureStub.demand.load {->}
        structureStub.demand.getData {->
            ConfigObject config = new ConfigObject()
            config.model = CoreModel
            config.periodCount = 1
            config.company.hierarchyLevel.components.hierarchyOutputComponent = new ConfigObject()

            return config
        }
        structureStub.demand.getName {-> "CoreStructure"}
    }

    void testWithModelStructureItem() {
        structureStub.use {
            ModelStructure modelStructure = new ModelStructure("CoreStructure")
            CoreModel model = new CoreModel()
            model.initComponents()
            StructureInformationInjector injector = new StructureInformationInjector(modelStructure, model)
            ConfigObject structure = injector.configObject

            assertSame structure.company.hierarchyLevel.components.hierarchyOutputComponent, model.hierarchyOutputComponent

        }
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

    //TODO: fix for with model
    /*void testInjectionDoesNotChangeStructure() {
        SparrowModel model = new SparrowModel()
        model.initComponents()
        StructureInformationInjector injector = new StructureInformationInjector("src/java/models/sparrow/SparrowStructure", model)
        ConfigObject structure = injector.configObject

        assertSame structure.company.Fire.components.frequencyGenerator, model.frequencyGenerator

        def frequencyGenerator = new FrequencyGenerator(parmDistribution: RandomDistributionFactory.getDistribution(FrequencyDistributionType.POISSON, ["lambda": 10]))
        model.frequencyGenerator = frequencyGenerator

        injector.injectConfiguration(model)

        assertSame frequencyGenerator, model.frequencyGenerator
    }

    void testRecursiveComponents() {
        Model model = new StructureTestModel()
        model.initComponents()
        StructureInformationInjector injector = new StructureInformationInjector("src/java/models/test/StructureTestStructure", model)
        ConfigObject structure = injector.configObject

        assertSame model.mtpl.subClaimsGenerator.subSingleClaimsGenerator.subFrequencyGenerator, structure.company.LOB1.components.mtpl.subClaimsGenerator.subSingleClaimsGenerator.subFrequencyGenerator
    }*/

}
