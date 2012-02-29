package org.pillarone.riskanalytics.core.components

import org.pillarone.riskanalytics.core.components.ComposedComponent
import org.pillarone.riskanalytics.core.packets.PacketList
import org.pillarone.riskanalytics.core.wiring.WiringUtils
import org.pillarone.riskanalytics.core.wiring.WireCategory
import org.pillarone.riskanalytics.core.wiring.PortReplicatorCategory
import org.pillarone.riskanalytics.core.packets.SingleValuePacket
import org.pillarone.riskanalytics.core.example.component.TestComponent2

/**
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
class TestStartComposedComponent extends ComposedComponent {

    PacketList<SingleValuePacket> input1 = new PacketList<SingleValuePacket>(SingleValuePacket)
    PacketList<SingleValuePacket> outValue1 = new PacketList<SingleValuePacket>(SingleValuePacket)
    PacketList<SingleValuePacket> outValue2 = new PacketList<SingleValuePacket>(SingleValuePacket)

    TestComponent2 subComponentA = new TestComponent2(name: 'A')
    TestComponent2 subComponentB = new TestComponent2(name: 'B')
    TestComponent2 subComponentC = new TestComponent2(name: 'C')

    @Override
    void wire() {
        WiringUtils.use(WireCategory) {
            subComponentC.input = subComponentB.outValue
        }
        WiringUtils.use(PortReplicatorCategory) {
            subComponentA.input = this.input1
            this.outValue1 = subComponentA.outValue
            this.outValue2 = subComponentC.outValue
        }
    }
}
