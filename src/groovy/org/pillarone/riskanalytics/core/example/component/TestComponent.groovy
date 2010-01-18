package org.pillarone.riskanalytics.core.example.component

import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.packets.PacketList
import org.pillarone.riskanalytics.core.example.packet.TestPacket
import org.pillarone.riskanalytics.core.wiring.ITransmitter

class TestComponent extends Component {

    PacketList input1
    PacketList input2
    PacketList<TestPacket> input3
    PacketList outValue1
    PacketList outValue2
    PacketList<TestPacket> outClaims
    PacketList expectedOutput1
    PacketList expectedOutput2

    boolean notifyTransmittedCalled = false
    boolean prepareOutputCalled = false
    boolean resetCalled = false
    double parmValue = 2.0

    public TestComponent() {
        input1 = new PacketList()
        input2 = new PacketList()
        input3 = new PacketList(TestPacket)
        outValue1 = new PacketList()
        outValue2 = new PacketList()
        outClaims = new PacketList(TestPacket)
        expectedOutput1 = new PacketList()
        expectedOutput2 = new PacketList()

    }

    public void doCalculation() {
        prepareOutputCalled = true
        outValue1.addAll(expectedOutput1);
        outValue2.addAll(expectedOutput2);
    }

    public void reset() {
        resetCalled = true
        super.reset()
    }

    public void notifyTransmitted(ITransmitter transmitter) {
        notifyTransmittedCalled = true
        super.notifyTransmitted(transmitter)
    }
}