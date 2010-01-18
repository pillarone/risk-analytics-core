package org.pillarone.riskanalytics.core.components

import org.pillarone.riskanalytics.core.packets.PacketList
import org.pillarone.riskanalytics.core.wiring.ITransmitter
import org.pillarone.riskanalytics.core.packets.TestPacketApple
import org.pillarone.riskanalytics.core.util.TestProbe

/**
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
class TransferDataToNextPeriodComponentTests extends GroovyTestCase {

    void testPublishOnlyIfUsedAsStartComponent() {
        TestTransferDataToNextPeriodComponent startComponent = new TestTransferDataToNextPeriodComponent()

        def probeTransferClaims = new TestProbe(startComponent, "outTestPacketApples")
        List transferClaims = probeTransferClaims.result

        startComponent.start()
        assertTrue "one packet sent", 1 == transferClaims.size()

        transferClaims.clear()

        startComponent.doCalculation()
        assertTrue "no packet sent", 0 == transferClaims.size()
    }

    // todo(sku): ask dko how this test case could be expanded in order to make sure that execute() is not called
    void testBlockDefaultNotifyWorkflow() {
        TestTransferDataToNextPeriodComponent startComponent = new TestTransferDataToNextPeriodComponent()

        def probeTransferClaims = new TestProbe(startComponent, "outTestPacketApples")
        List transferClaims = probeTransferClaims.result

        startComponent.allInputTransmitter.each {ITransmitter transmitter ->
            startComponent.notifyTransmitted transmitter
        }

        assertTrue "no packet sent", 0 == transferClaims.size()
    }
}

class TestTransferDataToNextPeriodComponent extends TransferDataToNextPeriodComponent {

    PacketList<TestPacketApple> inTestPacketApples = new PacketList(TestPacketApple)
    PacketList<TestPacketApple> outTestPacketApples = new PacketList(TestPacketApple)

    def simulationContext

    protected void doCalculation() {
        outTestPacketApples << new TestPacketApple(ultimate: 100, origin: this)
    }
}