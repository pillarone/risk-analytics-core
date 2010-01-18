package org.pillarone.riskanalytics.core.example.component

import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.components.PeriodStore
import org.pillarone.riskanalytics.core.packets.PacketList

class TestComponentWithSimulationContextAndPeriodStore extends Component {

    def simulationContext
    PeriodStore periodStore

    PacketList input = new PacketList()
    PacketList output = new PacketList()

    protected void doCalculation() {
        output.addAll input
    }
}