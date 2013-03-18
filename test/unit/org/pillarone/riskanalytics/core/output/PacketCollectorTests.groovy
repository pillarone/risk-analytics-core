package org.pillarone.riskanalytics.core.output

import models.core.CoreModel
import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.parameterization.StructureInformation
import org.pillarone.riskanalytics.core.parameterization.StructureInformationInjector
import org.pillarone.riskanalytics.core.wiring.ITransmitter

class PacketCollectorTests extends GroovyTestCase {

    void testDoNothingWhenNoPackets() {

        ICollectorOutputStrategy outputStrategy = [leftShift: {List results -> assertFalse("Output not to be called when no packets collected")}] as ICollectorOutputStrategy

        PacketCollector collector = new PacketCollector(outputStrategy: outputStrategy)

        collector.doCalculation()
    }

    void testAttachToModel() {
        CoreModel model = new CoreModel()
        model.init()


        PacketCollector collector = new PacketCollector()
        collector.path = "Core:exampleOutputComponent:outValue1"

        collector.attachToModel(model, null)

        assertEquals "#outTransmitter", 1, model.exampleOutputComponent.allOutputTransmitter.size()

        shouldFail(IllegalArgumentException) {
            collector.path = "TestModel:exampleOutputComponent:outValue1"
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
}