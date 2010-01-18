package org.pillarone.riskanalytics.core.example.component

import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.packets.SingleValuePacket
import org.pillarone.riskanalytics.core.packets.PacketList
import org.pillarone.riskanalytics.core.components.PeriodStore
import org.pillarone.riskanalytics.core.simulation.engine.PeriodScope

class TestComponentWithPeriodStore extends Component {

    public static final String PAID = "paid"

    public PacketList<SingleValuePacket> outPacket = new PacketList(SingleValuePacket)

    PeriodStore periodStore
    PeriodScope periodScope

    protected void doCalculation() {
        if (periodScope.currentPeriod == 0) {
            periodStore.put(PAID, new SingleValuePacket(value: 5d), PeriodStore.CURRENT_PERIOD)
            //idea:
            // periodStore.paid[0] = ...
            // periodStore.paid = ...
            outPacket << periodStore.get(PAID, PeriodStore.CURRENT_PERIOD)
            return
        }
        outPacket << periodStore.get(PAID, PeriodStore.LAST_PERIOD)
        // periodStore.paid[-1]
    }
}