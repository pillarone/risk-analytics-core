package org.pillarone.riskanalytics.core.example.component

import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.packets.PacketList
import org.pillarone.riskanalytics.core.packets.SingleValuePacket

class TestComponent2 extends Component {

    PacketList<SingleValuePacket> input = new PacketList<SingleValuePacket>(SingleValuePacket)
    PacketList<SingleValuePacket> outValue = new PacketList<SingleValuePacket>(SingleValuePacket)

    double parmValue = 2.0
    boolean doCalculationCalled = false

    public TestComponent2() {
    }

    public void doCalculation() {
        doCalculationCalled = true
        Double summedInput = (Double) input?.value?.sum()
        outValue << new SingleValuePacket(value: (summedInput ? summedInput : 1) * parmValue);
    }

}