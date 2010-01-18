package org.pillarone.riskanalytics.core.output

import models.core.CoreModel
import org.pillarone.riskanalytics.core.output.CollectorInformation
import org.pillarone.riskanalytics.core.output.PathMapping
import org.pillarone.riskanalytics.core.output.ResultConfigurationDAO
import org.pillarone.riskanalytics.core.example.component.ExampleInputOutputComponent
import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.parameterization.StructureInformation
import org.pillarone.riskanalytics.core.parameterization.StructureInformationInjector

class CollectorFactoryTests extends GroovyTestCase {

    void testCreateCollector() {

        ICollectorOutputStrategy outputStrategy = new FileOutput()

        CollectorFactory factory = new CollectorFactory(outputStrategy)

        CollectorInformation collectorInformation = new CollectorInformation()
        collectorInformation.path = new PathMapping(pathName: "myPath:outField")
        collectorInformation.collectingStrategyIdentifier = SingleValueCollectingModeStrategy.IDENTIFIER

        PacketCollector collector = factory.createCollector(collectorInformation)

        assertNotNull "no collector instance", collector
        assertSame "wrong strategy in collector", outputStrategy, collector.outputStrategy
        assertEquals "path", "myPath:outField", collector.path
        assertSame "collecting mode", SingleValueCollectingModeStrategy.IDENTIFIER, collector.mode.identifier

        assertNotSame "no new instance", collector, factory.createCollector(collectorInformation)
    }

    void testCreateCollectors() {

        Model testModel = new CoreModel()
        testModel.init()
        ICollectorOutputStrategy outputStrategy = new FileOutput()

        CollectorFactory factory = new CollectorFactory(outputStrategy)

        CollectorInformation collectorInformation1 = new CollectorInformation()
        collectorInformation1.path = new PathMapping(pathName: "Core:exampleOutputComponent:outValue1")
        collectorInformation1.collectingStrategyIdentifier = SingleValueCollectingModeStrategy.IDENTIFIER

        CollectorInformation collectorInformation2 = new CollectorInformation()
        collectorInformation2.path = new PathMapping(pathName: "Core:exampleOutputComponent:outValue2")
        collectorInformation2.collectingStrategyIdentifier = SingleValueCollectingModeStrategy.IDENTIFIER

        ResultConfigurationDAO configuration = new ResultConfigurationDAO()
        configuration.addToCollectorInformation(collectorInformation1)
        configuration.addToCollectorInformation(collectorInformation2)

        List collectors = factory.createCollectors(configuration, testModel)
        assertNotNull collectors
        assertFalse collectors.empty

    }

    void testCreateCollectorsWithStructureFile() {
        Model testModel = new CoreModel()
        testModel.init()
        testModel.injectComponentNames()

        ICollectorOutputStrategy outputStrategy = new FileOutput()

        CollectorFactory factory = new CollectorFactory(outputStrategy)
        factory.structureInformation = new StructureInformation(new StructureInformationInjector("src/java/models/core/CoreStructure", testModel).configObject, testModel)
        CollectorInformation collectorInformation1 = new CollectorInformation()
        collectorInformation1.path = new PathMapping(pathName: "Core:hierarchyLevel:hierarchyOutputComponent:outValue1")
        collectorInformation1.collectingStrategyIdentifier = SingleValueCollectingModeStrategy.IDENTIFIER

        CollectorInformation collectorInformation2 = new CollectorInformation()
        collectorInformation2.path = new PathMapping(pathName: "Core:hierarchyLevel:hierarchyOutputComponent:outValue2")
        collectorInformation2.collectingStrategyIdentifier = SingleValueCollectingModeStrategy.IDENTIFIER

        ResultConfigurationDAO configuration = new ResultConfigurationDAO()
        configuration.addToCollectorInformation(collectorInformation1)
        configuration.addToCollectorInformation(collectorInformation2)

        List collectors = factory.createCollectors(configuration, testModel)
        assertNotNull collectors
        assertFalse collectors.empty
    }

    void testCreateCollectors_Empty() {

        Model testModel = new CoreModel()
        testModel.init()

        ICollectorOutputStrategy outputStrategy = new FileOutput()

        CollectorFactory factory = new CollectorFactory(outputStrategy)

        ResultConfigurationDAO configuration = new ResultConfigurationDAO()

        List collectors = factory.createCollectors(configuration, testModel)
        assertNotNull collectors
        assertTrue collectors.empty
    }

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

        CollectorInformation collectorInformationContainingWildCard = new CollectorInformation()
        collectorInformationContainingWildCard.path = new PathMapping(pathName: "Core:dynamicComponent:subSubcomponent:outValue")
        collectorInformationContainingWildCard.collectingStrategyIdentifier = SingleValueCollectingModeStrategy.IDENTIFIER

        List enhancedCollectorInformation = factory.enhanceCollectorInformationSet([collectorInformationContainingWildCard], model)
        assertEquals "# collectorInformation", 2, enhancedCollectorInformation.size()
        assertFalse "wildcard not removed", enhancedCollectorInformation.contains(collectorInformationContainingWildCard)

    }
}
