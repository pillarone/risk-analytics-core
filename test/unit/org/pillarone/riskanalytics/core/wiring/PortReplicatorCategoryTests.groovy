package org.pillarone.riskanalytics.core.wiring

import org.pillarone.riskanalytics.core.components.ComposedComponent
import org.pillarone.riskanalytics.core.packets.PacketList
import org.pillarone.riskanalytics.core.example.component.TestComponent
import org.pillarone.riskanalytics.core.example.packet.TestPacketOther

class PortReplicatorCategoryTests extends GroovyTestCase {

    TestCompoundComponent compound
    TestComponent outerComponent
    TestComponent subComponent

    void testReplicateInput() {
        compound = new TestCompoundComponent()

        WiringUtils.use(PortReplicatorCategory) {
            compound.subComponent1.input1 = compound.inPort1
        }

        assertSame(compound, compound.subComponent1.allInputTransmitter[0].sender)
        assertSame(compound.subComponent1, compound.subComponent1.allInputTransmitter[0].receiver)
        assertSame(compound.inPort1, compound.subComponent1.allInputTransmitter[0].source)
        assertSame(compound.subComponent1.input1, compound.subComponent1.allInputTransmitter[0].target)

        assertSame(compound, compound.allInputReplicationTransmitter[0].sender)
        assertSame(compound.subComponent1, compound.allInputReplicationTransmitter[0].receiver)
        assertSame(compound.inPort1, compound.allInputReplicationTransmitter[0].source)
        assertSame(compound.subComponent1.input1, compound.allInputReplicationTransmitter[0].target)
    }

    void testReplicateOutput() {
        compound = new TestCompoundComponent()

        WiringUtils.use(PortReplicatorCategory) {
            compound.outPort1 = compound.subComponent1.outValue1
        }

        assertEquals "output replication uses SilentTransmitter", SilentTransmitter, compound.subComponent1.allOutputTransmitter[0].class


        assertTrue("no input transmitter added for out replication", compound.allInputTransmitter.empty)

        assertSame(compound.subComponent1, compound.allOutputReplicationTransmitter[0].sender)
        assertSame(compound, compound.allOutputReplicationTransmitter[0].receiver)
        assertSame(compound.subComponent1.outValue1, compound.allOutputReplicationTransmitter[0].source)
        assertSame(compound.outPort1, compound.allOutputReplicationTransmitter[0].target)

        assertSame(compound.subComponent1, compound.subComponent1.allOutputTransmitter[0].sender)
        assertSame(compound, compound.subComponent1.allOutputTransmitter[0].receiver)
        assertSame(compound.subComponent1.outValue1, compound.subComponent1.allOutputTransmitter[0].source)
        assertSame(compound.outPort1, compound.subComponent1.allOutputTransmitter[0].target)
    }


    void testOnlySubComponentPortsCanBeReplicated() {
        compound = new TestCompoundComponent()
        outerComponent = new TestComponent()

        WiringUtils.use(PortReplicatorCategory) {
            compound.outPort1 = compound.subComponent1.outValue1
            shouldFail(java.lang.UnsupportedOperationException, {outerComponent.outValue1 = compound.outPort1})
            shouldFail(java.lang.UnsupportedOperationException, {compound.inPort1 = outerComponent.input1})
        }
    }

    void testOnlyMatchingPortsCanBeReplicated() {
        compound = new TestCompoundComponent()

        WiringUtils.use(PortReplicatorCategory) {
            compound.outPort1 = compound.subComponent1.outValue1
            compound.subComponent1.input1 = compound.inPort1
            shouldFail(java.lang.UnsupportedOperationException, {compound.subComponent1.input1 = compound.outPort1})
            shouldFail(java.lang.UnsupportedOperationException, {compound.outPort1 = compound.subComponent1.input1})
        }
    }

    void testDirectionOfReplication() {
        compound = new TestCompoundComponent()

        WiringUtils.use(PortReplicatorCategory) {
            compound.outPort1 = compound.subComponent1.outValue1
            compound.subComponent1.input1 = compound.inPort1
            // input can only be replicated into the compoundComponent [compound.in -> subComponen.in]
            shouldFail(java.lang.UnsupportedOperationException, {compound.inPort1 = compound.subComponent1.input1})
            // output can only be replicated out of the compoundComponent [subComponen.out -> compound.out]
            shouldFail(java.lang.UnsupportedOperationException, {compound.subComponent1.outValue1 = compound.outPort1})
        }
    }

    void testDifferentPortTypes() {
        compound = new TestCompoundComponent()

        WiringUtils.use(PortReplicatorCategory) {
            shouldFail(java.lang.IllegalArgumentException, {compound.subComponent1.input3 = compound.inPort2})
        }
    }

    void testSubComponentInList() {
        compound = new TestCompoundComponent()
        subComponent = new TestComponent()
        compound.components << subComponent


        WiringUtils.use(PortReplicatorCategory) {
            compound.outPort1 = subComponent.outValue1
        }

        assertTrue("no input transmitter added for out replication", compound.allInputTransmitter.empty)

        assertSame(subComponent, compound.allOutputReplicationTransmitter[0].sender)
        assertSame(compound, compound.allOutputReplicationTransmitter[0].receiver)
        assertSame(subComponent.outValue1, compound.allOutputReplicationTransmitter[0].source)
        assertSame(compound.outPort1, compound.allOutputReplicationTransmitter[0].target)

        assertSame(subComponent, subComponent.allOutputTransmitter[0].sender)
        assertSame(compound, subComponent.allOutputTransmitter[0].receiver)
        assertSame(subComponent.outValue1, subComponent.allOutputTransmitter[0].source)
        assertSame(compound.outPort1, subComponent.allOutputTransmitter[0].target)

    }

}

class TestCompoundComponent extends ComposedComponent {

    PacketList inPort1
    PacketList outPort1
    PacketList<TestPacketOther> inPort2 = new PacketList(TestPacketOther)

    TestComponent subComponent1
    TestComponent subComponent2
    List components

    public TestCompoundComponent() {
        inPort1 = new PacketList()
        outPort1 = new PacketList()
        subComponent1 = new TestComponent()
        subComponent2 = new TestComponent()
        components = []
    }

    public void wire() {

    }

}