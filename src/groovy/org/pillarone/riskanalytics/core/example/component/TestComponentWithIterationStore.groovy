package org.pillarone.riskanalytics.core.example.component

import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.packets.PacketList
import org.pillarone.riskanalytics.core.packets.SingleValuePacket
import org.pillarone.riskanalytics.core.components.IterationStore
import org.pillarone.riskanalytics.core.simulation.engine.IterationScope

class TestComponentWithIterationStore extends Component {

    public static final String PAID = "paid"

    public PacketList<SingleValuePacket> outPacket = new PacketList(SingleValuePacket)

    IterationStore iterationStore
    IterationScope iterationScope

    protected void doCalculation() {
        if (iterationScope.periodScope.isFirstPeriod()) {
            iterationStore.put(PAID, new SingleValuePacket(value: 5d), IterationStore.CURRENT_PERIOD)
            //idea:
            // periodStore.paid[0] = ...
            // periodStore.paid = ...
            outPacket << iterationStore.get(PAID, IterationStore.CURRENT_PERIOD)
            return
        }
        outPacket << iterationStore.get(PAID, IterationStore.LAST_PERIOD)
        // periodStore.paid[-1]
    }
}