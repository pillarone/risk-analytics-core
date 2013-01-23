package org.pillarone.riskanalytics.core.components

import org.pillarone.riskanalytics.core.packets.Packet
import org.pillarone.riskanalytics.core.wiring.ITransmitter
import org.pillarone.riskanalytics.core.wiring.Transmitter
import org.pillarone.riskanalytics.core.example.component.TestComponent

class ComponentTests extends GroovyTestCase {


    void testExecuteTransmitsOnOutputTransmitter() {
        Component component1 = new TestComponent()
        boolean transmitted = false
        component1.allOutputTransmitter << ([transmit: {-> transmitted = true}] as ITransmitter)

        component1.start()

        assertTrue(transmitted)

    }

    void testPrepareOutputCalledOnAllTransmittedInputs() {
        Component component1 = new TestComponent()
        assertFalse("initial condition", component1.prepareOutputCalled)
        boolean transmitter1Transmitted = false
        boolean transmitter2Transmitted = false
        ITransmitter transmitter1 = [transmit: {-> }, isTransmitted: {-> return transmitter1Transmitted}, setTransmitted: {boolean isTransmitted -> transmitter1Transmitted = isTransmitted}] as ITransmitter
        ITransmitter transmitter2 = [transmit: {-> }, isTransmitted: {-> return transmitter2Transmitted}, setTransmitted: {boolean isTransmitted -> transmitter1Transmitted = isTransmitted}] as ITransmitter
        component1.allInputTransmitter << transmitter1
        component1.allInputTransmitter << transmitter2

        component1.notifyTransmitted(null)
        assertFalse("no execute -  transmitCount < #transmitter", component1.prepareOutputCalled)
        component1.notifyTransmitted(null)
        assertTrue("execute - transmitCount == #transmitter", component1.prepareOutputCalled)

    }

    void testInputTransmitterResetOnExecute() {
        Component component1 = new TestComponent()
        boolean inputTransmitted = true
        component1.allInputTransmitter << ([transmit: {-> }, isTransmitted: {-> return inputTransmitted}, setTransmitted: {boolean isTransmitted -> inputTransmitted = isTransmitted}] as ITransmitter)
        component1.start()
        assertFalse("input transmitter not reset", inputTransmitted)
    }

    void testOutputTransmitterNotResetOnExecute() {
        Component component1 = new TestComponent()
        boolean outputTransmitted = true
        component1.allOutputTransmitter << ([transmit: {-> }, isTransmitted: {-> return outputTransmitted}, setTransmitted: {boolean isTransmitted -> outputTransmitted = isTransmitted}] as ITransmitter)
        component1.start()
        assertTrue("output transmitter reset", outputTransmitted)
    }

    void testResetComponentOnExecute() {
        Component component1 = new TestComponent()
        component1.outValue1 << new Packet()
        assertFalse("initial condition", component1.resetCalled)
        component1.start()
        assertTrue(component1.outValue1.empty)
        assertTrue("reset called after execute", component1.resetCalled)
    }

    void testIsSenderWired() {
        Component component1 = new TestComponent()
        Component component2 = new TestComponent()

        Transmitter transmitter1 = new Transmitter(component1, component1.outValue1, component2, component2.input1)
        component1.allOutputTransmitter << transmitter1
        assertTrue("out - in 1 connected", component1.isSenderWired(component1.outValue1))
        assertFalse("out - in 2 connected", component1.isSenderWired(component1.outValue2))
    }

    void testIsReceiverWired() {
        Component component1 = new TestComponent()
        Component component2 = new TestComponent()

        Transmitter transmitter1 = new Transmitter(component1, component1.outValue1, component2, component2.input1)
        component2.allInputTransmitter << transmitter1
        assertTrue("out - in 1 connected", component2.isReceiverWired(component2.input1))
        assertFalse("out - in 2 connected", component2.isReceiverWired(component2.input2))
    }

    void testWiredReceivers() {
        Component component1 = new TestComponent()
        Component component2 = new TestComponent()

        Transmitter transmitter1 = new Transmitter(component1, component1.outValue1, component2, component2.input1)
        component1.allOutputTransmitter << transmitter1
        component2.allInputTransmitter << transmitter1
        assertTrue("input1 wired", 1 == component1.wiredReceivers(component1.outValue1))
        assertTrue("input2 not wired", 0 == component1.wiredReceivers(component1.outValue2))
    }

    void testOneReceiversWired() {
        Component component1 = new TestComponent()
        Component component2 = new TestComponent()

        Transmitter transmitter1 = new Transmitter(component1, component1.outValue1, component2, component2.input1)
        component1.allOutputTransmitter << transmitter1
        component2.allInputTransmitter << transmitter1
        assertTrue("input1 wired", component1.isOneReceiverWired(component1.outValue1))
        assertFalse("input2 not wired", component1.isOneReceiverWired(component1.outValue2))
    }

    void testGetParameterizationProperties() {
        Component component1 = new TestComponent()
        List list = component1.allParameterizationProperties()
        assertEquals "one parm property", 1, list.size()
        assertEquals "correct property value", 2.0, list.get(0).value
    }

    void testNumberOfWiredInChannels() {
        Component component1 = new TestComponent()
        Component component2 = new TestComponent()
        assertEquals "component 1: no in channels wired", 0, component1.numberOfWiredInChannels()
        assertEquals "component 2: no in channels wired", 0, component2.numberOfWiredInChannels()
        assertFalse component1.hasWiredInChannels()
        assertFalse component2.hasWiredInChannels()

        Transmitter transmitter1 = new Transmitter(component1, component1.outValue1, component2, component2.input1)
        component2.allInputTransmitter << transmitter1
        assertEquals "component 1: no in channels wired after first wiring", 0, component1.numberOfWiredInChannels()
        assertEquals "component 2: 1 in channels wired", 1, component2.numberOfWiredInChannels()
        assertFalse component1.hasWiredInChannels()
        assertTrue component2.hasWiredInChannels()

        Transmitter transmitter2 = new Transmitter(component1, component1.outValue1, component2, component2.input1)
        component2.allInputTransmitter << transmitter2
        assertEquals "component 1: no in channels wired after second wiring", 0, component1.numberOfWiredInChannels()
        assertEquals "component 2: 1 in channels wired", 1, component2.numberOfWiredInChannels()
        assertFalse component1.hasWiredInChannels()
        assertTrue component2.hasWiredInChannels()

        Transmitter transmitter3 = new Transmitter(component1, component1.outValue1, component2, component2.input2)
        component2.allInputTransmitter << transmitter3
        assertEquals "component 1: no in channels wired after third wiring", 0, component1.numberOfWiredInChannels()
        assertEquals "component 2: 2 in channels wired", 2, component2.numberOfWiredInChannels()
        assertFalse component1.hasWiredInChannels()
        assertTrue component2.hasWiredInChannels()
    }
}



