package org.pillarone.riskanalytics.core.example.component

import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.packets.PacketList
import org.pillarone.riskanalytics.core.wiring.ITransmitter

class TestSubComponent extends Component {
    PacketList input1
    PacketList input2
    PacketList outValue1
    PacketList outValue2

    public TestSubComponent() {
        input1 = new PacketList()
        input2 = new PacketList()
        outValue1 = new PacketList()
        outValue2 = new PacketList()
    }

    public void notifyTransmitted(ITransmitter transmitter) {
        super.notifyTransmitted(transmitter);    //To change body of overridden methods use File | Settings | File Templates.
    }


    public void doCalculation() {
        outValue1.addAll(input1)
        outValue2.addAll(input2)
    }
}