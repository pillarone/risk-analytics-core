package org.pillarone.riskanalytics.core.wiring

import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.packets.Packet
import org.pillarone.riskanalytics.core.example.component.TestComponent

class TransmitterTests extends GroovyTestCase {

    void testTransmit() {
        Component sender = new TestComponent()
        Component receiver = new TestComponent()
        Packet transmittedPacket = new Packet()
        sender.outValue1 << transmittedPacket
        assertTrue("no input values", receiver.input1.empty)

        ITransmitter transmitter = new Transmitter(sender, sender.outValue1, receiver, receiver.input1)

        transmitter.transmit()

        assertSame("values transmitted", transmittedPacket, receiver.input1[0])
        assertNotSame("only value reference transmitted", sender.outValue1, receiver.input1)
        assertTrue(transmitter.transmitted)
        assertTrue(receiver.notifyTransmittedCalled)
        assertSame(sender, transmittedPacket.sender)
        assertEquals("outValue1", transmittedPacket.senderChannelName)
    }

    void testNoRetransmission() {
        Component sender = new TestComponent()
        Component receiver = new TestComponent()
        Packet transmittedPacket = new Packet()
        sender.outValue1 << transmittedPacket
        assertTrue("no input values", receiver.input1.empty)

        ITransmitter transmitter1 = new Transmitter(sender, sender.outValue1, receiver, receiver.input1)

        transmitter1.transmit()
        assertSame("values transmitted", transmittedPacket, receiver.input1[0])
        shouldFail(java.lang.IllegalStateException, {transmitter1.transmit()})
    }

    public void testConstructor() {
        Component sender = new TestComponent()
        Component receiver = new TestComponent()
        ITransmitter transmitter = new Transmitter(sender, sender.outValue1, receiver, receiver.input1)
        assertEquals "senderChannelName", "outValue1", transmitter.senderChannelName
    }
}