package org.pillarone.riskanalytics.core.wiring

import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.packets.Packet
import org.pillarone.riskanalytics.core.components.TestComponent

class WiringTests extends GroovyTestCase {
    Component sender
    /* This component is used to avoid that the receiver can be executed once it
     * has received all input. As the execution of the receiver would reset it
     * and the input properties would be null thereafter. */
    Component senderBlocker
    Component receiver

    protected void setUp() {
        super.setUp()
        sender = new TestComponent()
        senderBlocker = new TestComponent()
        receiver = new TestComponent()
    }

    void testWiringSender() {

        assertEquals(0, sender.allOutputTransmitter.size())

        WiringUtils.use(WireCategory) {
            receiver.input1 = sender.outValue1
        }
        assertEquals(1, sender.allOutputTransmitter.size())

    }

    void testWiringReceiver() {

        assertTrue(receiver.allInputTransmitter.isEmpty())

        WiringUtils.use(WireCategory) {
            receiver.input1 = sender.outValue1
        }

        assertEquals(1, receiver.allInputTransmitter.size())
        assertSame(sender, receiver.allInputTransmitter[0].sender)
        assertSame(receiver, receiver.allInputTransmitter[0].receiver)
        assertSame("input property on transmitter", receiver.input1, receiver.allInputTransmitter[0].target)
        assertSame("output property on transmitter", sender.outValue1, receiver.allInputTransmitter[0].source)
    }

    void testNotifyReceiver() {

        List input = receiver.input1
        assertTrue("input empty", input.isEmpty())

        WiringUtils.use(WireCategory) {
            receiver.input1 = sender.outValue1
            receiver.input1 = senderBlocker.outValue1
        }

        Packet packet = new Packet()
        sender.expectedOutput1 << packet
        sender.start()
        assertSame("input not reinitialized", input, receiver.input1)
        assertSame("value transmitted", packet, receiver.input1[0])
    }


    void testWiringOfTwoOutputs() {

        assertEquals 0, sender.allOutputTransmitter.size()
        Packet packet1 = new Packet()
        sender.expectedOutput1 << packet1
        Packet packet2 = new Packet()
        sender.expectedOutput2 << packet2
        WiringUtils.use(WireCategory) {
            receiver.input1 = sender.outValue1
            receiver.input2 = sender.outValue2
            receiver.input1 = senderBlocker.outValue1
            receiver.input2 = senderBlocker.outValue2
        }
        assertEquals 2, sender.allOutputTransmitter.size()

        sender.start()

        assertSame("value transmitted to input 1", packet1, receiver.input1[0])
        assertSame("value transmitted to input 2", packet2, receiver.input2[0])
    }

    void testPropertyAccessDuringWiring() {

        Packet packet = new Packet()
        sender.input1 << packet

        WiringUtils.use(WireCategory) {
            // category tranforms all access to fields starting with 'out' into a LinkedProperty object.
            LinkedProperty connector = (LinkedProperty) sender.outValue1
            assertSame(sender, connector.source)
            assertEquals("outProperty name in LinkedProperty", "outValue1", connector.name)

            // basic field access
            assertSame(packet, sender.input1[0])
        }


    }

     void testTypeIncompatibleWiring() {
        sender = new TestComponent()
        receiver = new TestComponent()

        WiringUtils.use(WireCategory) {
            shouldFail(WiringException,
                {
                    receiver.inApple = sender.outOrange
                }
            )
        }
    }


}