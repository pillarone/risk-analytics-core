package org.pillarone.riskanalytics.core.output

import models.core.CoreModel
import org.junit.Test
import org.pillarone.riskanalytics.core.example.component.ExampleInputOutputComponent
import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.parameterization.StructureInformation
import org.pillarone.riskanalytics.core.parameterization.StructureInformationInjector
import org.pillarone.riskanalytics.core.simulation.item.ResultConfiguration

import static org.junit.Assert.*

class CollectorFactoryTests {

    @Test
    void testCreateCollector() {

        ICollectorOutputStrategy outputStrategy = new FileOutput()

        CollectorFactory factory = new CollectorFactory(outputStrategy)

        PacketCollector collectorInformation = new PacketCollector()
        collectorInformation.path = "myPath:outField"
        collectorInformation.mode = CollectingModeFactory.getStrategy(SingleValueCollectingModeStrategy.IDENTIFIER)

        PacketCollector collector = factory.createCollector(collectorInformation)

        assertNotNull "no collector instance", collector
        assertSame "wrong strategy in collector", outputStrategy, collector.outputStrategy
        assertEquals "path", "myPath:outField", collector.path
        assertSame "collecting mode", SingleValueCollectingModeStrategy.IDENTIFIER, collector.mode.identifier
    }

    @Test
    void testCreateCollectors() {

        Model testModel = new CoreModel()
        testModel.init()
        ICollectorOutputStrategy outputStrategy = new FileOutput()

        CollectorFactory factory = new CollectorFactory(outputStrategy)

        PacketCollector collectorInformation1 = new PacketCollector()
        collectorInformation1.path = "Core:exampleOutputComponent:outValue1"
        collectorInformation1.mode = CollectingModeFactory.getStrategy(SingleValueCollectingModeStrategy.IDENTIFIER)

        PacketCollector collectorInformation2 = new PacketCollector()
        collectorInformation2.path = "Core:exampleOutputComponent:outValue2"
        collectorInformation2.mode = CollectingModeFactory.getStrategy(SingleValueCollectingModeStrategy.IDENTIFIER)

        ResultConfiguration configuration = new ResultConfiguration("test", CoreModel)
        configuration.collectors << collectorInformation1
        configuration.collectors << collectorInformation2

        List collectors = factory.createCollectors(configuration, testModel)
        assertNotNull collectors
        assertFalse collectors.empty

    }

    @Test
    void testCreateCollectorsWithStructureFile() {
        Model testModel = new CoreModel()
        testModel.init()
        testModel.injectComponentNames()

        ICollectorOutputStrategy outputStrategy = new FileOutput()

        CollectorFactory factory = new CollectorFactory(outputStrategy)
        factory.structureInformation = new StructureInformation(new StructureInformationInjector("src/java/models/core/CoreStructure", testModel).configObject, testModel)

        PacketCollector collectorInformation1 = new PacketCollector()
        collectorInformation1.path = "Core:hierarchyLevel:hierarchyOutputComponent:outValue1"
        collectorInformation1.mode = CollectingModeFactory.getStrategy(SingleValueCollectingModeStrategy.IDENTIFIER)

        PacketCollector collectorInformation2 = new PacketCollector()
        collectorInformation2.path = "Core:hierarchyLevel:hierarchyOutputComponent:outValue2"
        collectorInformation2.mode = CollectingModeFactory.getStrategy(SingleValueCollectingModeStrategy.IDENTIFIER)

        ResultConfiguration configuration = new ResultConfiguration("test", CoreModel)
        configuration.collectors << collectorInformation1
        configuration.collectors << collectorInformation2


        List collectors = factory.createCollectors(configuration, testModel)
        assertNotNull collectors
        assertFalse collectors.empty
    }

    @Test
    void testCreateCollectors_Empty() {

        Model testModel = new CoreModel()
        testModel.init()

        ICollectorOutputStrategy outputStrategy = new FileOutput()

        CollectorFactory factory = new CollectorFactory(outputStrategy)

        ResultConfiguration configuration = new ResultConfiguration("name", CoreModel)

        List collectors = factory.createCollectors(configuration, testModel)
        assertNotNull collectors
        assertTrue collectors.empty
    }

    @Test
    void testEnhanceCollectorInformation() {

        CoreModel model = new CoreModel()
        model.init()
        ExampleInputOutputComponent line1 = model.dynamicComponent.createDefaultSubComponent()
        line1.name = "line1"
        model.dynamicComponent.addSubComponent(line1)

        ExampleInputOutputComponent line2 = model.dynamicComponent.createDefaultSubComponent()
        line2.name = "line2"
        model.dynamicComponent.addSubComponent(line2)

        CollectorFactory factory = new CollectorFactory(new FileOutput())

        PacketCollector collectorInformationContainingWildCard = new PacketCollector()
        collectorInformationContainingWildCard.path = "Core:dynamicComponent:subSubcomponent:outValue"
        collectorInformationContainingWildCard.mode = CollectingModeFactory.getStrategy(SingleValueCollectingModeStrategy.IDENTIFIER)

        List enhancedCollectorInformation = factory.enhanceCollectorInformationSet([collectorInformationContainingWildCard], model)
        assertEquals "# collectorInformation", 2, enhancedCollectorInformation.size()
        assertFalse "wildcard not removed", enhancedCollectorInformation.contains(collectorInformationContainingWildCard)

    }

    @Test
    void testNestedComponentInDynamicComponent() {
        CoreModel model = new CoreModel()
        model.init()
        ExampleInputOutputComponent line1 = model.dynamicComponent.createDefaultSubComponent()
        line1.name = "subLine1"
        model.dynamicComponent.addSubComponent(line1)

        CollectorFactory factory = new CollectorFactory(new FileOutput())

        PacketCollector collectorInformationContainingWildCard = new PacketCollector()
        collectorInformationContainingWildCard.path = "Core:dynamicComponent:subSubcomponent:subSomething:outValue"
        collectorInformationContainingWildCard.mode = CollectingModeFactory.getStrategy(SingleValueCollectingModeStrategy.IDENTIFIER)


        List enhancedCollectorInformation = factory.enhanceCollectorInformationSet([collectorInformationContainingWildCard], model)
        assertEquals "# collectorInformation", 1, enhancedCollectorInformation.size()
        //PMO-734: this line tests if out channels of a sub component of a dynamically added subcomponent are resolved correctly
        assertEquals "Core:dynamicComponent:subLine1:subSomething:outValue", enhancedCollectorInformation[0].path
    }
}
