package org.pillarone.riskanalytics.core.simulation.item

import models.core.CoreModel
import org.pillarone.riskanalytics.core.fileimport.ResultConfigurationImportService
import org.pillarone.riskanalytics.core.output.*

class ResultConfigurationTests extends GroovyTestCase {

    void testLoad() {
        new ResultConfigurationImportService().compareFilesAndWriteToDB(['Core'])

        String configName = "CoreResultConfiguration"
        ResultConfiguration configuration = new ResultConfiguration(configName)
        configuration.modelClass = CoreModel
        configuration.load()

        assertEquals CoreModel.name, configuration.modelClass.name
        assertEquals configName, configuration.name
        assertEquals "1", configuration.versionNumber.toString()

        assertEquals 4, configuration.collectors.size()

        List<PacketCollector> sortedCollectors = configuration.collectors.sort {PacketCollector pc -> pc.path }
        assertEquals AggregatedCollectingModeStrategy.IDENTIFIER, sortedCollectors.get(0).mode.identifier
        assertEquals AggregatedCollectingModeStrategy.IDENTIFIER, sortedCollectors.get(1).mode.identifier
        assertEquals SingleValueCollectingModeStrategy.IDENTIFIER, sortedCollectors.get(2).mode.identifier
        assertEquals SingleValueCollectingModeStrategy.IDENTIFIER, sortedCollectors.get(3).mode.identifier
    }

    void testSave() {

        int initialConfigurationCount = ResultConfigurationDAO.count()
        int initialCollectorCount = CollectorInformation.count()

        ResultConfiguration configuration = new ResultConfiguration("newConfig")
        configuration.modelClass = CoreModel

        configuration.collectors << new PacketCollector(path: "CoreModel:exampleInputOutputComponent:outValue", mode: CollectingModeFactory.getStrategy(SingleValueCollectingModeStrategy.IDENTIFIER))
        configuration.collectors << new PacketCollector(path: "CoreModel:exampleOutputComponent:outValue1", mode: CollectingModeFactory.getStrategy(AggregatedCollectingModeStrategy.IDENTIFIER))
        configuration.collectors << new PacketCollector(path: "CoreModel:exampleOutputComponent:outValue2", mode: CollectingModeFactory.getStrategy(SingleValueCollectingModeStrategy.IDENTIFIER))

        long id = configuration.save()
        assertNotNull id

        assertEquals initialConfigurationCount + 1, ResultConfigurationDAO.count()
        assertEquals initialCollectorCount + 3, CollectorInformation.count()

        configuration.load()

        assertEquals 3, configuration.collectors.size()

        List<PacketCollector> sortedCollectors = configuration.collectors.sort {PacketCollector pc -> pc.path }
        assertEquals SingleValueCollectingModeStrategy.IDENTIFIER, sortedCollectors.get(0).mode.identifier
        assertEquals AggregatedCollectingModeStrategy.IDENTIFIER, sortedCollectors.get(1).mode.identifier
        assertEquals SingleValueCollectingModeStrategy.IDENTIFIER, sortedCollectors.get(2).mode.identifier

        //Test updating of existing collector infos

        configuration.collectors.clear()
        configuration.collectors << new PacketCollector(path: "CoreModel:exampleInputOutputComponent:outValue", mode: CollectingModeFactory.getStrategy(AggregatedCollectingModeStrategy.IDENTIFIER))
        configuration.collectors << new PacketCollector(path: "CoreModel:exampleOutputComponent:outValue1", mode: CollectingModeFactory.getStrategy(SingleValueCollectingModeStrategy.IDENTIFIER))
        configuration.collectors << new PacketCollector(path: "CoreModel:exampleOutputComponent:outValue2", mode: CollectingModeFactory.getStrategy(AggregatedCollectingModeStrategy.IDENTIFIER))

        id = configuration.save()
        assertNotNull id

        assertEquals initialConfigurationCount + 1, ResultConfigurationDAO.count()
        assertEquals initialCollectorCount + 3, CollectorInformation.count()

        configuration.load()

        assertEquals 3, configuration.collectors.size()

        sortedCollectors = configuration.collectors.sort {PacketCollector pc -> pc.path }
        assertEquals AggregatedCollectingModeStrategy.IDENTIFIER, sortedCollectors.get(0).mode.identifier
        assertEquals SingleValueCollectingModeStrategy.IDENTIFIER, sortedCollectors.get(1).mode.identifier
        assertEquals AggregatedCollectingModeStrategy.IDENTIFIER, sortedCollectors.get(2).mode.identifier

        //Test removing a collector info

        configuration.collectors.clear()
        configuration.collectors << new PacketCollector(path: "CoreModel:exampleInputOutputComponent:outValue", mode: CollectingModeFactory.getStrategy(AggregatedCollectingModeStrategy.IDENTIFIER))
        configuration.collectors << new PacketCollector(path: "CoreModel:exampleOutputComponent:outValue1", mode: CollectingModeFactory.getStrategy(SingleValueCollectingModeStrategy.IDENTIFIER))

        id = configuration.save()
        assertNotNull id

        assertEquals initialConfigurationCount + 1, ResultConfigurationDAO.count()
        assertEquals initialCollectorCount + 2, CollectorInformation.count()

        configuration.load()

        assertEquals 2, configuration.collectors.size()

        sortedCollectors = configuration.collectors.sort {PacketCollector pc -> pc.path }
        assertEquals AggregatedCollectingModeStrategy.IDENTIFIER, sortedCollectors.get(0).mode.identifier
        assertEquals SingleValueCollectingModeStrategy.IDENTIFIER, sortedCollectors.get(1).mode.identifier

        //Test add a new collector info

        configuration.collectors.clear()
        configuration.collectors << new PacketCollector(path: "CoreModel:exampleInputOutputComponent:outValue", mode: CollectingModeFactory.getStrategy(AggregatedCollectingModeStrategy.IDENTIFIER))
        configuration.collectors << new PacketCollector(path: "CoreModel:exampleOutputComponent:outValue1", mode: CollectingModeFactory.getStrategy(SingleValueCollectingModeStrategy.IDENTIFIER))
        configuration.collectors << new PacketCollector(path: "CoreModel:exampleOutputComponent:outValue2", mode: CollectingModeFactory.getStrategy(SingleValueCollectingModeStrategy.IDENTIFIER))

        id = configuration.save()
        assertNotNull id

        assertEquals initialConfigurationCount + 1, ResultConfigurationDAO.count()
        assertEquals initialCollectorCount + 3, CollectorInformation.count()

        configuration.load()

        assertEquals 3, configuration.collectors.size()

        sortedCollectors = configuration.collectors.sort {PacketCollector pc -> pc.path }
        assertEquals AggregatedCollectingModeStrategy.IDENTIFIER, sortedCollectors.get(0).mode.identifier
        assertEquals SingleValueCollectingModeStrategy.IDENTIFIER, sortedCollectors.get(1).mode.identifier
        assertEquals SingleValueCollectingModeStrategy.IDENTIFIER, sortedCollectors.get(2).mode.identifier
    }

    void testFileConfiguration() {

        ConfigObject config = new ConfigSlurper().parse(new File("src/java/models/core/CoreResultConfiguration.groovy").toURI().toURL())

        assertNotNull config

        assertSame SingleValueCollectingModeStrategy.IDENTIFIER, config.components.exampleOutputComponent.outValue1

        assertSame AggregatedCollectingModeStrategy.IDENTIFIER, config.components.exampleInputOutputComponent.outValue

    }

    void testConfigObject() {
        new ResultConfigurationImportService().compareFilesAndWriteToDB(['Core'])

        String configName = "CoreResultConfiguration"
        ResultConfiguration configuration = new ResultConfiguration(configName)
        configuration.modelClass = CoreModel
        configuration.load()

        ConfigObject configObject = configuration.toConfigObject()
        assertNotNull configObject

        assertEquals CoreModel.name, configObject.model.name
        assertEquals configuration.name, configObject.displayName

        assertEquals AggregatedCollectingModeStrategy.IDENTIFIER, configObject.components.dynamicComponent.subSubcomponent.outValue
        assertEquals AggregatedCollectingModeStrategy.IDENTIFIER, configObject.components.exampleInputOutputComponent.outValue
        assertEquals SingleValueCollectingModeStrategy.IDENTIFIER, configObject.components.exampleOutputComponent.outValue1
        assertEquals SingleValueCollectingModeStrategy.IDENTIFIER, configObject.components.exampleOutputComponent.outValue2

        ResultConfiguration newConfiguration = new ResultConfiguration(configObject, "name")
        assertEquals configuration.collectors.size(), newConfiguration.collectors.size()
        for (int i = 0; i < configuration.collectors.size(); i++) {
            PacketCollector old = configuration.collectors.sort { it.path }.get(i)
            PacketCollector new_ = newConfiguration.collectors.sort { it.path }.get(i)

            assertEquals old.path, new_.path
            assertEquals old.mode.identifier, new_.mode.identifier
        }
        assertEquals configuration.modelClass, newConfiguration.modelClass
        assertEquals configuration.name, newConfiguration.name
    }

    void testSetCollector() {
        String configName = "CoreResultConfiguration"
        ResultConfiguration configuration = new ResultConfiguration(configName)
        AggregatedCollectingModeStrategy strategy = new AggregatedCollectingModeStrategy()
        AggregatedCollectingModeStrategy newStrategy = new AggregatedCollectingModeStrategy()
        configuration.setCollector("path", strategy)
        assert configuration.getCollector("path").mode == strategy
        configuration.setCollector("path",null)
        assert 0 == configuration.collectors.size()
        assert !configuration.getCollector("path")
        configuration.setCollector("path",strategy)
        configuration.setCollector("path",newStrategy)
        assert 1 == configuration.collectors.size()
        assert configuration.getCollector("path").mode == newStrategy
        assert 1 == configuration.collectors.size()
    }

}
