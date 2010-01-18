package org.pillarone.riskanalytics.core.output

import grails.test.GrailsUnitTestCase
import models.core.CoreModel
import org.pillarone.riskanalytics.core.example.model.TestModel
import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.parameterization.StructureInformation
import org.pillarone.riskanalytics.core.parameterization.StructureInformationInjector
import org.pillarone.riskanalytics.core.wiring.ITransmitter

class PacketCollectorTests extends GrailsUnitTestCase {

    void testDoNothingWhenNoPackets() {

        ICollectorOutputStrategy outputStrategy = [leftShift: {List results -> assertFalse("Output not to be called when no packets collected")}] as ICollectorOutputStrategy

        PacketCollector collector = new PacketCollector(outputStrategy: outputStrategy)

        collector.doCalculation()
    }

    void testAttachToModel() {
        TestModel model = new TestModel()
        model.init()


        PacketCollector collector = new PacketCollector()
        collector.path = "Test:frequencyGenerator:outFrequency"

        collector.attachToModel(model, null)

        assertEquals "#outTransmitter", 1, model.frequencyGenerator.allOutputTransmitter.size()

        shouldFail(IllegalArgumentException) {
            collector.path = "TestModel:frequencyGenerator:outFrequency"
            collector.attachToModel(model, null)
        }

    }

    void testAttachToModelWithStructure() {
        Model testModel = new CoreModel()
        testModel.init()
        testModel.injectComponentNames()

        StructureInformation structureInformation = new StructureInformation(new StructureInformationInjector("src/java/models/core/CoreStructure", testModel).configObject, testModel)

        PacketCollector collector = new PacketCollector(path: "Core:hierarchyLevel:hierarchyOutputComponent:outValue1")

        collector.attachToModel(testModel, structureInformation)

        assertEquals 1, testModel.hierarchyOutputComponent.allOutputTransmitter.size()
        ITransmitter transmitter = testModel.hierarchyOutputComponent.allOutputTransmitter.get(0)
        assertSame collector, transmitter.receiver

    }

/* // mockDomain causing StackOverflow
    void testCreateResults() {
        mockDomain PathMapping
        mockDomain CollectorMapping
        mockDomain FieldMapping
        mockDomain SingleValueResult

        FieldMapping fieldMapping = new FieldMapping(fieldName: "ultimate")
        fieldMapping.save()
        PathMapping pathMapping = new PathMapping(pathName: "path")
        pathMapping.save()
        CollectorMapping collectorMapping = new CollectorMapping(collectorName: "collector")
        collectorMapping.save()

        PeriodScope periodScope = new PeriodScope()
        IterationScope iterationScope = new IterationScope(periodScope:periodScope)
        SimulationScope simulationScope = new SimulationScope(iterationScope: iterationScope)
        periodScope.currentPeriod = 1
        iterationScope.currentIteration = 13

        PacketCollector collector = new PacketCollector(simulationScope:simulationScope)
        collector.path = "path"
        collector.collectorName = "collector"

        Claim claim = new Claim(value: 1.3d)

        List results = collector.createResults(claim.valuesToSave)
        assertNotNull results
        assertEquals 1, results.size()
        SingleValueResult result = results[0]

        assertNotNull result.simulationRun
        assertEquals "period", 1, result.period
        assertEquals "iteration", 1, result.iteration
        assertSame "pathMapping", pathMapping, result.path
        assertSame "collector", collectorMapping, result.collector
        assertSame "field", fieldMapping, result.field
        assertEquals "valueIndex", 0, result.valueIndex
        assertEquals "value", 1.3d, result.value

    }
*/
}