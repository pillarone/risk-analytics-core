package org.pillarone.riskanalytics.core.example.component

import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.packets.PacketList
import org.pillarone.riskanalytics.core.packets.SingleValuePacket
import org.pillarone.riskanalytics.core.components.ResourceHolder


class ExampleComponentContainingResource extends Component {

    PacketList<SingleValuePacket> input = new PacketList<SingleValuePacket>(SingleValuePacket)
    PacketList<SingleValuePacket> output = new PacketList<SingleValuePacket>(SingleValuePacket)

    ResourceHolder<ExampleResource> parmResource = new ResourceHolder<ExampleResource>(ExampleResource)

    @Override
    protected void doCalculation() {
        for (SingleValuePacket inPacket: input) {
            output << new SingleValuePacket(inPacket.value * parmResource.resource.parmInteger)
        }
    }
}