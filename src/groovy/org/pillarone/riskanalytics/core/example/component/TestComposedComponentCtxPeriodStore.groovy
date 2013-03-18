package org.pillarone.riskanalytics.core.example.component

import org.pillarone.riskanalytics.core.components.ComposedComponent
import org.pillarone.riskanalytics.core.packets.PacketList
import org.pillarone.riskanalytics.core.wiring.WiringUtils
import org.pillarone.riskanalytics.core.wiring.WireCategory
import org.pillarone.riskanalytics.core.wiring.PortReplicatorCategory

class TestComposedComponentCtxPeriodStore extends ComposedComponent {

    TestComponentWithSimulationContext subComponent1 = new TestComponentWithSimulationContext()
    TestComponentWithSimulationContextAndPeriodStore subComponent2 = new TestComponentWithSimulationContextAndPeriodStore()

    PacketList input = new PacketList()
    PacketList output = new PacketList()

    public void wire() {

        WiringUtils.use(WireCategory) {
            subComponent2.input = subComponent1.output
        }
        WiringUtils.use(PortReplicatorCategory) {
            subComponent1.input = this.input
            this.output = subComponent2.output
        }
    }
}