package org.pillarone.riskanalytics.core.components

import org.pillarone.riskanalytics.core.packets.TestPacketApple
import org.pillarone.riskanalytics.core.packets.PacketList
import org.pillarone.riskanalytics.core.packets.TestPacketOrange

/**
 * @author fouad.jaada@intuitive-collaboration.com
 */
class TestComponent extends Component {

    PacketList<TestPacketApple> inApple = new PacketList<TestPacketApple>(TestPacketApple)
    PacketList<TestPacketOrange> outOrange = new PacketList<TestPacketOrange>(TestPacketOrange)
    PacketList input1
    PacketList input2
    PacketList outValue1
    PacketList outValue2
    PacketList<TestPacketApple> outClaims
    PacketList expectedOutput1
    PacketList expectedOutput2

    boolean notifyTransmittedCalled = false
    boolean prepareOutputCalled = false
    boolean resetCalled = false
    double parmValue = 2.0

    public TestComponent() {
        input1 = new PacketList()
        input2 = new PacketList()
        outValue1 = new PacketList()
        outValue2 = new PacketList()
        outClaims = new PacketList(TestPacketApple)
        expectedOutput1 = new PacketList()
        expectedOutput2 = new PacketList()

    }


    protected void doCalculation() {
        prepareOutputCalled = true
        outValue1.addAll(expectedOutput1);
        outValue2.addAll(expectedOutput2);
    }

    public void reset() {
        resetCalled = true
        super.reset()
    }

}
