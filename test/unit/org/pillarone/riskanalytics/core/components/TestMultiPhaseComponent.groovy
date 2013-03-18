package org.pillarone.riskanalytics.core.components

import org.pillarone.riskanalytics.core.packets.TestPacketOrange
import org.pillarone.riskanalytics.core.packets.PacketList
import org.pillarone.riskanalytics.core.packets.TestPacketApple

/**
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
class TestMultiPhaseComponent extends MultiPhaseComponent {

    PacketList<TestPacketApple> inApplePhase1 = new PacketList<TestPacketApple>(TestPacketApple)
    PacketList<TestPacketOrange> inOrangePhase1 = new PacketList<TestPacketOrange>(TestPacketOrange)

    PacketList<TestPacketOrange> inOrangePhase2 = new PacketList<TestPacketOrange>(TestPacketOrange)

    PacketList<TestPacketApple> outApplePhase1 = new PacketList<TestPacketApple>(TestPacketApple)
    PacketList<TestPacketOrange> outOrangePhase1 = new PacketList<TestPacketOrange>(TestPacketOrange)

    PacketList<TestPacketOrange> outOrangePhase2 = new PacketList<TestPacketOrange>(TestPacketOrange)

    private static final String PHASE1 = "Phase 1"
    private static final String PHASE2 = "Phase 2"

    void allocateChannelsToPhases() {
        setTransmitterPhaseInput inApplePhase1, PHASE1
        setTransmitterPhaseInput inOrangePhase1, PHASE1
        setTransmitterPhaseInput inOrangePhase2, PHASE2
        setTransmitterPhaseOutput outApplePhase1, PHASE1
        setTransmitterPhaseOutput outOrangePhase1, PHASE1
        setTransmitterPhaseOutput outOrangePhase2, PHASE2
    }

    void doCalculation(String phase) {
        if (phase.equals(PHASE1)) {
            outApplePhase1.addAll inApplePhase1
            outOrangePhase1.addAll inOrangePhase1
        }
        else if (phase.equals(PHASE2)) {
            outOrangePhase2.addAll inOrangePhase2
        }
    }
}
