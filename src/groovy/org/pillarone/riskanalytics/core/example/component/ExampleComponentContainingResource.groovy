package org.pillarone.riskanalytics.core.example.component

import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.packets.PacketList
import org.pillarone.riskanalytics.core.packets.SingleValuePacket


class ExampleComponentContainingResource extends Component {

    PacketList<SingleValuePacket> input = new PacketList<SingleValuePacket>(SingleValuePacket)
    PacketList<SingleValuePacket> output = new PacketList<SingleValuePacket>(SingleValuePacket)

    ExampleResource parmResource = new ExampleResource()

    @Override
    protected void doCalculation() {
        for (SingleValuePacket inPacket : input) {
            output << inPacket.value * parmResource.parmInteger
        }
    }
}