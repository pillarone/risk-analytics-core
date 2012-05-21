package org.pillarone.riskanalytics.core.components

import org.pillarone.riskanalytics.core.packets.PacketList
import org.pillarone.riskanalytics.core.packets.TestPacketApple
import org.pillarone.riskanalytics.core.packets.TestPacketOrange
import org.pillarone.riskanalytics.core.wiring.WiringUtils
import org.pillarone.riskanalytics.core.wiring.WireCategory
import org.pillarone.riskanalytics.core.wiring.PortReplicatorCategory

/**
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
class TestMultiPhaseComposedComponent extends MultiPhaseComposedComponent {

    PacketList<TestPacketApple> inApplePhase1 = new PacketList<TestPacketApple>(TestPacketApple)
    PacketList<TestPacketOrange> inOrangePhase1 = new PacketList<TestPacketOrange>(TestPacketOrange)

    PacketList<TestPacketOrange> inOrangePhase2 = new PacketList<TestPacketOrange>(TestPacketOrange)

    PacketList<TestPacketApple> outApplePhase1 = new PacketList<TestPacketApple>(TestPacketApple)
    PacketList<TestPacketOrange> outOrangePhase1 = new PacketList<TestPacketOrange>(TestPacketOrange)

    PacketList<TestPacketOrange> outOrangePhase2 = new PacketList<TestPacketOrange>(TestPacketOrange)

    TestMultiPhaseComponent sub1 = new TestMultiPhaseComponent(name: '1')
    TestMultiPhaseComponent sub2 = new TestMultiPhaseComponent(name: '2')

    private static final String PHASE1 = "Phase 1";
    private static final String PHASE2 = "Phase 2";

    void allocateChannelsToPhases() {
        setTransmitterPhaseInput inApplePhase1, PHASE1
        setTransmitterPhaseInput inOrangePhase1, PHASE1
        setTransmitterPhaseInput inOrangePhase2, PHASE2
        setTransmitterPhaseOutput outApplePhase1, PHASE1
        setTransmitterPhaseOutput outOrangePhase1, PHASE1
        setTransmitterPhaseOutput outOrangePhase2, PHASE2
    }

    protected void doCalculation(String phase) {
        sub1.doCalculation phase
        sub2.doCalculation phase
    }

    void wire() {
        WiringUtils.use(WireCategory) {
            sub2.inApplePhase1 = sub1.outApplePhase1
            sub2.inOrangePhase1 = sub1.outOrangePhase1
            sub2.inOrangePhase2 = sub1.outOrangePhase2
        }
        WiringUtils.use(PortReplicatorCategory) {
            sub1.inApplePhase1 = this.inApplePhase1
            sub1.inOrangePhase1 = this.inOrangePhase1
            sub1.inOrangePhase2 = this.inOrangePhase2
            this.outApplePhase1= sub2.outApplePhase1
            this.outOrangePhase1 = sub2.outOrangePhase1
            this.outOrangePhase2 = sub2.outOrangePhase2
        }
    }
}