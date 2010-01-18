package org.pillarone.riskanalytics.core.packets

import org.pillarone.riskanalytics.core.packets.Packet
import org.pillarone.riskanalytics.core.packets.PacketList

class PacketListTests extends GroovyTestCase {

    void testConstructor() {
        PacketList<TestPacketApple> testPacketApples = new PacketList(TestPacketApple)
        testPacketApples.add(new TestPacketApple())
        assertEquals 1, testPacketApples.size()

        PacketList packets = new PacketList()
        packets.isCompatibleTo(new PacketList(Packet))

        packets = new PacketList([new TestPacketOrange()])
        packets.isCompatibleTo(new PacketList(Packet))

        packets = new PacketList([new TestPacketApple()])
        packets.isCompatibleTo(new PacketList(Packet))

        // TODO (msh): Do mixed packets make sense ?
        packets = new PacketList([new TestPacketApple(), new TestPacketOrange()])
        packets.isCompatibleTo(new PacketList(Packet))

        packets = new PacketList(new PacketList(TestPacketApple))
        packets.isCompatibleTo(new PacketList(Packet))
        packets.isCompatibleTo(new PacketList(TestPacketApple))

    }

    void testIncompatibleType() {
        PacketList<TestPacketApple> testPacketApples = new PacketList(TestPacketApple)
        shouldFail java.lang.IllegalArgumentException, {testPacketApples.add(new TestPacketOrange())}
        shouldFail java.lang.IllegalArgumentException, {testPacketApples.add(0, new TestPacketOrange())}
        shouldFail java.lang.IllegalArgumentException, {testPacketApples.addAll([new TestPacketOrange()])}
        shouldFail java.lang.IllegalArgumentException, {testPacketApples.addAll(0, [new TestPacketOrange()])}

        shouldFail java.lang.IllegalArgumentException, {PacketList<String> packetListWithString = new PacketList(String)}

    }

    void testIsCompatibleList() {
        PacketList<Packet> packets = new PacketList(Packet)
        PacketList<TestPacketApple> testPacketApples = new PacketList(TestPacketApple)
        PacketList<TestPacketApple> otherPacketApples = new PacketList(TestPacketApple)
        PacketList<TestPacketOrange> testPacketOranges = new PacketList(TestPacketOrange)

        assertTrue("Element Claim is subtype", packets.isCompatibleTo(otherPacketApples))
        assertTrue("Element Frequency is subtype", packets.isCompatibleTo(testPacketOranges))

        assertTrue("Element matches type", testPacketApples.isCompatibleTo(otherPacketApples))

        assertFalse("Element type mismatch", testPacketOranges.isCompatibleTo(otherPacketApples))
        assertFalse("Element type mismatch", testPacketOranges.isCompatibleTo(packets))
    }

    void testAddElements() {
        PacketList<TestPacketApple> testPacketApples = new PacketList(TestPacketApple)
        PacketList<TestPacketApple> otherPacketApples = new PacketList(TestPacketApple)

        otherPacketApples.add(new TestPacketApple())
        assertEquals(1, otherPacketApples.size())

        TestPacketApple testPacketApple = new TestPacketApple()
        testPacketApples.add(0, testPacketApple)

        assertEquals(1, testPacketApples.size())
        testPacketApples.addAll(otherPacketApples)
        assertEquals(2, testPacketApples.size())
    }
}