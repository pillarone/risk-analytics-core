package org.pillarone.riskanalytics.core.components

import org.pillarone.riskanalytics.core.packets.TestPacketApple
import org.pillarone.riskanalytics.core.packets.TestPacketOrange
import org.pillarone.riskanalytics.core.util.TestProbe
import org.pillarone.riskanalytics.core.wiring.WireCategory
import org.pillarone.riskanalytics.core.wiring.WiringUtils

/**
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
class MultiPhaseComposedComponentTests extends GroovyTestCase {

    TestMultiPhaseComponent sourceComponent = new TestMultiPhaseComponent(name: 'source')
    TestMultiPhaseComponent sourceComponent2 = new TestMultiPhaseComponent(name: 'source')
    TestMultiPhaseComponent targetComponent = new TestMultiPhaseComponent(name: 'target')
    TestMultiPhaseComposedComponent composedComponent = new TestMultiPhaseComposedComponent(name: 'composite')
    
    void testUsage() {
        composedComponent.internalWiring()
        WiringUtils.use(WireCategory) {
            composedComponent.inApplePhase1 = sourceComponent.outApplePhase1
            composedComponent.inOrangePhase1 = sourceComponent.outOrangePhase1
            composedComponent.inOrangePhase2 = sourceComponent.outOrangePhase2
            targetComponent.inApplePhase1 = composedComponent.outApplePhase1
            targetComponent.inOrangePhase1 = composedComponent.outOrangePhase1
            targetComponent.inOrangePhase2 = composedComponent.outOrangePhase2
        }
        composedComponent.optimizeWiring()
        composedComponent.allocateChannelsToPhases()

        TestPacketOrange orangeA = new TestPacketOrange()
        TestPacketOrange orangeB = new TestPacketOrange()
        TestPacketOrange orangeC = new TestPacketOrange()
        TestPacketOrange orangeD = new TestPacketOrange()
        TestPacketApple appleA = new TestPacketApple()
        TestPacketApple appleB = new TestPacketApple()

        sourceComponent.inApplePhase1 << appleA << appleB
        sourceComponent.inOrangePhase1 << orangeA << orangeB << orangeC
        sourceComponent.inOrangePhase2 << orangeD

        List targetComponentApplePhase1 = new TestProbe(targetComponent, 'outApplePhase1').result
        List targetComponentOrangePhase1 = new TestProbe(targetComponent, 'outOrangePhase1').result
        List targetComponentOrangePhase2 = new TestProbe(targetComponent, 'outOrangePhase2').result

        sourceComponent.calculateAndPublish("Phase 1")
        assertEquals 'targetComponent.outApplePhase1.size', 2, targetComponentApplePhase1.size()
        assertEquals 'targetComponent.outOrangePhase1.size', 3, targetComponentOrangePhase1.size()
        assertEquals 'targetComponent.outOrangePhase2.size', 0, targetComponentOrangePhase2.size()

        sourceComponent.calculateAndPublish("Phase 2")
        assertEquals 'targetComponent.outApplePhase1.size', 2, targetComponentApplePhase1.size()
        assertEquals 'targetComponent.outOrangePhase1.size', 3, targetComponentOrangePhase1.size()
        assertEquals 'targetComponent.outOrangePhase2.size', 1, targetComponentOrangePhase2.size()
    }

    // https://issuetracking.intuitive-collaboration.com/jira/browse/PMO-1733
    void testMultiInChannels() {
        composedComponent.internalWiring()
        WiringUtils.use(WireCategory) {
            composedComponent.inApplePhase1 = sourceComponent.outApplePhase1
            composedComponent.inOrangePhase1 = sourceComponent.outOrangePhase1
            composedComponent.inOrangePhase2 = sourceComponent.outOrangePhase2
            composedComponent.inApplePhase1 = sourceComponent2.outApplePhase1
            composedComponent.inOrangePhase1 = sourceComponent2.outOrangePhase1
            composedComponent.inOrangePhase2 = sourceComponent2.outOrangePhase2
            targetComponent.inApplePhase1 = composedComponent.outApplePhase1
            targetComponent.inOrangePhase1 = composedComponent.outOrangePhase1
            targetComponent.inOrangePhase2 = composedComponent.outOrangePhase2
        }
        composedComponent.optimizeWiring()
        composedComponent.allocateChannelsToPhases()

        TestPacketOrange orangeA = new TestPacketOrange()
        TestPacketOrange orangeB = new TestPacketOrange()
        TestPacketOrange orangeC = new TestPacketOrange()
        TestPacketOrange orangeD = new TestPacketOrange()
        TestPacketApple appleA = new TestPacketApple()
        TestPacketApple appleB = new TestPacketApple()

        sourceComponent.inApplePhase1 << appleA << appleB
        sourceComponent.inOrangePhase1 << orangeA << orangeB << orangeC
        sourceComponent.inOrangePhase2 << orangeD
        sourceComponent2.inApplePhase1 << appleA << appleB
        sourceComponent2.inOrangePhase1 << orangeA << orangeB << orangeC
        sourceComponent2.inOrangePhase2 << orangeD

        List targetComponentApplePhase1 = new TestProbe(targetComponent, 'outApplePhase1').result
        List targetComponentOrangePhase1 = new TestProbe(targetComponent, 'outOrangePhase1').result
        List targetComponentOrangePhase2 = new TestProbe(targetComponent, 'outOrangePhase2').result

        sourceComponent.calculateAndPublish("Phase 1")
        sourceComponent2.calculateAndPublish("Phase 1")
        assertEquals 'targetComponent.outApplePhase1.size', 4, targetComponentApplePhase1.size()
        assertEquals 'targetComponent.outOrangePhase1.size', 6, targetComponentOrangePhase1.size()
        assertEquals 'targetComponent.outOrangePhase2.size', 0, targetComponentOrangePhase2.size()

        sourceComponent.calculateAndPublish("Phase 2")
        sourceComponent2.calculateAndPublish("Phase 2")
        assertEquals 'targetComponent.outApplePhase1.size', 4, targetComponentApplePhase1.size()
        assertEquals 'targetComponent.outOrangePhase1.size', 6, targetComponentOrangePhase1.size()
        assertEquals 'targetComponent.outOrangePhase2.size', 2, targetComponentOrangePhase2.size()
    }
}
