package org.pillarone.riskanalytics.core.wiring

import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.packets.Packet
import org.pillarone.riskanalytics.core.example.component.TestComponent

class SilentTransmitterTests extends GroovyTestCase {

    void testTransmit() {
        Component sender = new TestComponent()
        Component receiver = new TestComponent()
        Packet transmittedPacket = new Packet()
        sender.outValue1 << transmittedPacket
        assertTrue("no input values", receiver.outValue2.empty)

        ITransmitter transmitter = new SilentTransmitter(sender, sender.outValue1, receiver, receiver.outValue2)

        transmitter.transmit()

        assertSame("values transmitted", transmittedPacket, receiver.outValue2[0])
        assertNotSame("only value reference transmitted", sender.outValue1, receiver.outValue2)
        assertTrue(transmitter.transmitted)
        assertFalse("SilentTransmitter do not notify the receiver", receiver.notifyTransmittedCalled)
        assertSame(sender, transmittedPacket.sender)
        assertEquals("outValue1", transmittedPacket.senderChannelName)
    }

    void testNoRetransmission() {
        Component sender = new TestComponent()
        Component receiver = new TestComponent()
        Packet transmittedPacket = new Packet()
        sender.outValue1 << transmittedPacket
        assertTrue("no input values", receiver.input1.empty)

        ITransmitter transmitter1 = new SilentTransmitter(sender, sender.outValue1, receiver, receiver.input1)

        transmitter1.transmit()
        assertSame("values transmitted", transmittedPacket, receiver.input1[0])
        shouldFail(java.lang.IllegalStateException, {transmitter1.transmit()})


    }
}