package org.pillarone.riskanalytics.core.components

import org.pillarone.riskanalytics.core.components.ComposedComponent
import org.pillarone.riskanalytics.core.packets.PacketList
import org.pillarone.riskanalytics.core.packets.SingleValuePacket
import org.pillarone.riskanalytics.core.wiring.PortReplicatorCategory
import org.pillarone.riskanalytics.core.wiring.WireCategory
import org.pillarone.riskanalytics.core.wiring.WiringUtils

/**
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
class TestStartNestedComposedComponent extends ComposedComponent {

    PacketList<SingleValuePacket> outValue1 = new PacketList<SingleValuePacket>(SingleValuePacket)
    PacketList<SingleValuePacket> outValue2 = new PacketList<SingleValuePacket>(SingleValuePacket)

    TestComponent2 subComponentA = new TestComponent2(name: 'a')
    TestComponent2 subComponentB = new TestComponent2(name: 'b')
    TestStartComposedComponent subComponentC = new TestStartComposedComponent(name: 'c')

    @Override
    void wire() {
        WiringUtils.use(WireCategory) {
            subComponentB.input = subComponentA.outValue
            subComponentB.input = subComponentC.outValue2
        }
        WiringUtils.use(PortReplicatorCategory) {
            this.outValue1 = subComponentC.outValue1
            this.outValue2 = subComponentB.outValue
        }
    }
}
