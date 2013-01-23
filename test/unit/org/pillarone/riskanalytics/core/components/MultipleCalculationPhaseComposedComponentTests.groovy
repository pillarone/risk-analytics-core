package org.pillarone.riskanalytics.core.components


import org.pillarone.riskanalytics.core.packets.PacketList
import org.pillarone.riskanalytics.core.wiring.PortReplicatorCategory
import org.pillarone.riskanalytics.core.wiring.WireCategory
import org.pillarone.riskanalytics.core.wiring.WiringUtils
import org.pillarone.riskanalytics.core.packets.MultiValuePacket
import org.pillarone.riskanalytics.core.util.TestProbe
import org.pillarone.riskanalytics.core.packets.TestCommissionsPaid

/**
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
class MultipleCalculationPhaseComposedComponentTests extends GroovyTestCase {

    TestMCPHelperComponent followingComponent1
    TestMCPHelperComponent providerComponent2
    TestMCPHelperComponent followingComponent2
    TestMultipleCalculationPhaseComposedComponent component

    protected void setUp() {
        super.setUp();
        followingComponent1 = new TestMCPHelperComponent(name: "followingComponent1")
        providerComponent2 = new TestMCPHelperComponent(name: "providerComponent2")
        followingComponent2 = new TestMCPHelperComponent(name: "followingComponent2")
        component = new TestMultipleCalculationPhaseComposedComponent()
    }

    void testUsage() {
        WiringUtils.use(WireCategory) {
            component.input2 = providerComponent2.outValue
            followingComponent1.input = component.outValue1
            followingComponent2.input = component.outValue2
        }

        component.internalWiring()
        component.optimizeWiring()
        component.allocateChannelsToPhases()

        List outValue1 = new TestProbe(followingComponent1, "outValue").result
        List outValue2 = new TestProbe(followingComponent2, "outValue").result


        assertTrue("outValue1 empty before start", outValue1.isEmpty())
        assertTrue("outValue2 empty before start", outValue2.isEmpty())

        TestCommissionsPaid testCommissionsPaid = new TestCommissionsPaid()
        component.subComponent1.input << testCommissionsPaid

        component.start()

        assertSame("input1 and output1 have to be the same", testCommissionsPaid, outValue1[0])
        assertTrue("outValue2 empty before start", outValue2.isEmpty())

        TestCommissionsPaid testCommissionsPaid1 = new TestCommissionsPaid()
        providerComponent2.input << testCommissionsPaid1
        providerComponent2.execute()

        assertSame("output2 have to be the same", testCommissionsPaid1, outValue2[0])
    }
}

class TestMultipleCalculationPhaseComposedComponent extends MultipleCalculationPhaseComposedComponent {

    PacketList<MultiValuePacket> input1 = new PacketList(MultiValuePacket.class)
    PacketList<MultiValuePacket> input2 = new PacketList(MultiValuePacket.class)

    PacketList<MultiValuePacket> outValue1 = new PacketList(MultiValuePacket.class)
    PacketList<MultiValuePacket> outValue2 = new PacketList(MultiValuePacket.class)

    TestMCPHelperComponent subComponent1 = new TestMCPHelperComponent(name: "subComponent1")
    TestMCPHelperComponent subComponent2 = new TestMCPHelperComponent(name: "subComponent2")

    public void allocateChannelsToPhases() {
        setTransmitterPhaseInput(input1, MultipleCalculationPhaseComposedComponent.PHASE_START)
        setTransmitterPhaseInput(input2, MultipleCalculationPhaseComposedComponent.PHASE_DO_CALCULATION)
        setTransmitterPhaseOutput(outValue1, MultipleCalculationPhaseComposedComponent.PHASE_START)
        setTransmitterPhaseOutput(outValue2, MultipleCalculationPhaseComposedComponent.PHASE_DO_CALCULATION)
    }

    public void wire() {
        WiringUtils.use(PortReplicatorCategory) {
            subComponent1.input = this.input1
            subComponent2.input = this.input2
            this.outValue1 = subComponent1.outValue
            this.outValue2 = subComponent2.outValue
        }
    }
}

class TestMCPHelperComponent extends Component {
    PacketList<MultiValuePacket> input = new PacketList(MultiValuePacket.class)
    PacketList<MultiValuePacket> outValue = new PacketList(MultiValuePacket.class)

    public TestSubComponent() {
        input = new PacketList()
        outValue = new PacketList()
    }

    public void doCalculation() {
        outValue.addAll(input)
    }
}