package org.pillarone.riskanalytics.core.example.component

import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.packets.PacketList
import org.pillarone.riskanalytics.core.packets.Packet
import org.pillarone.riskanalytics.core.example.marker.ITestComponentMarker

class ExampleOutputComponent extends Component implements ITestComponentMarker {

    PacketList<Packet> outValue1 = new PacketList<Packet>()
    PacketList<Packet> outValue2 = new PacketList<Packet>()

    protected void doCalculation() {

    }


}