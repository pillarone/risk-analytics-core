package org.pillarone.riskanalytics.core.example.component

import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.packets.PacketList

class TestComponentWithSimulationContext extends Component {

    def simulationContext
    def simulationScope

    PacketList input = new PacketList()
    PacketList output = new PacketList()

    protected void doCalculation() {
        output.addAll input
    }
}