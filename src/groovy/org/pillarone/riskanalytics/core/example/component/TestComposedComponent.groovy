package org.pillarone.riskanalytics.core.example.component

import org.pillarone.riskanalytics.core.components.ComposedComponent
import org.pillarone.riskanalytics.core.packets.PacketList
import org.pillarone.riskanalytics.core.wiring.WiringUtils
import org.pillarone.riskanalytics.core.wiring.WireCategory
import org.pillarone.riskanalytics.core.wiring.PortReplicatorCategory

class TestComposedComponent extends ComposedComponent {

    PacketList input1
    PacketList input2

    PacketList outValue1
    PacketList outValue2

    TestSubComponent subComponent1
    TestSubComponent subComponent2

    public TestComposedComponent() {
        input1 = new PacketList()
        input2 = new PacketList()

        outValue1 = new PacketList()
        outValue2 = new PacketList()

        subComponent1 = new TestSubComponent(name: "subComponent1")
        subComponent2 = new TestSubComponent(name: "subComponent2")
    }



    public void wire() {
        WiringUtils.use(WireCategory) {
            subComponent2.input1 = subComponent1.outValue1
            subComponent2.input2 = subComponent1.outValue2
        }
        WiringUtils.use(PortReplicatorCategory) {
            subComponent1.input1 = this.input1
            subComponent1.input2 = this.input2
            this.outValue1 = subComponent2.outValue1
            this.outValue2 = subComponent2.outValue2
        }
    }

}