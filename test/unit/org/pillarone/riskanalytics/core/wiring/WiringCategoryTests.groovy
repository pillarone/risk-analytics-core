package org.pillarone.riskanalytics.core.wiring

import org.pillarone.riskanalytics.core.packets.Packet
import org.pillarone.riskanalytics.core.packets.PacketList
import org.pillarone.riskanalytics.core.packets.TestPacketApple
import org.pillarone.riskanalytics.core.example.component.TestComponent

class WiringCategoryTests extends GroovyTestCase {

    void testDoGetProperty() {
        TestComponent component = new TestComponent()
        Object anyProperty = WireCategory.doGetProperty(component, "input1")
        assertEquals(component.input1, anyProperty)

        LinkedProperty outProperty = WireCategory.doGetProperty(component, "outValue1")
        assertSame(component, outProperty.source)
        assertEquals("outValue1", outProperty.name)
    }

    void testDoSetProperty() {
        TestComponent sender = new TestComponent()
        TestComponent receiver = new TestComponent()
        PacketList input = receiver.input1

        LinkedProperty property = new LinkedProperty(source: sender, name: "input2")
        assertTrue(receiver.allInputTransmitter.isEmpty())
        assertEquals(0, sender.allOutputTransmitter.size())
        WireCategory.doSetProperty(receiver, "input1", property)
        assertEquals("wiring creates an input transmitter on receiver", 1, receiver.allInputTransmitter.size())
        assertEquals("wiring creates an output transmitter on sender", 1, sender.allOutputTransmitter.size())

        assertSame("original property unchanged", input, receiver.input1)


        def newValue = new PacketList()
        WireCategory.doSetProperty(receiver, "expectedOutput1", newValue)
        assertSame("only 'in' properties are wired", newValue, receiver.expectedOutput1)

    }

    void testInputOutputWithDifferentTypes() {
        TestComponent sender = new TestComponent()
        TestComponent receiver = new TestComponent()
        sender.outValue1 = new PacketList(TestPacketApple)
        receiver.input1 = new PacketList(Packet)

        LinkedProperty property = new LinkedProperty(source: sender, name: "outValue1")
        WireCategory.doSetProperty(receiver, "input1", property)

        sender.outValue1 = new PacketList()
        receiver.input1 = new PacketList(TestPacketApple)
        // elements of sender's packetList do not fit into receivers input PacketList
        shouldFail(IllegalArgumentException, {WireCategory.doSetProperty(receiver, "input1", property)})

    }

}