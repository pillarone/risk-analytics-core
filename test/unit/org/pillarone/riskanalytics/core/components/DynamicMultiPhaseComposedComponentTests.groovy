package org.pillarone.riskanalytics.core.components

import org.pillarone.riskanalytics.core.wiring.WireCategory
import org.pillarone.riskanalytics.core.wiring.WiringUtils
import org.pillarone.riskanalytics.core.packets.TestPacketApple
import org.pillarone.riskanalytics.core.packets.TestPacketOrange
import org.pillarone.riskanalytics.core.util.TestProbe

/**
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
class DynamicMultiPhaseComposedComponentTests extends GroovyTestCase {

    TestMultiPhaseComponent sourceComponent = new TestMultiPhaseComponent(name: 'source')
    TestMultiPhaseComponent sourceComponent2 = new TestMultiPhaseComponent(name: 'source')
    TestMultiPhaseComponent targetComponent = new TestMultiPhaseComponent(name: 'target')
    TestMultiPhaseComponent sub1 = new TestMultiPhaseComponent(name: 'dyn.sub1')
    TestMultiPhaseComponent sub2 = new TestMultiPhaseComponent(name: 'dyn.sub2')
    TestDynamicMultiPhaseComposedComponent dynamicComponent = new TestDynamicMultiPhaseComposedComponent(name: 'dyn')
    
    void testUsage() {
        dynamicComponent.addSubComponent sub1
        dynamicComponent.addSubComponent sub2
        dynamicComponent.internalWiring()
        WiringUtils.use(WireCategory) {
            dynamicComponent.inApplePhase1 = sourceComponent.outApplePhase1
            dynamicComponent.inOrangePhase1 = sourceComponent.outOrangePhase1
            dynamicComponent.inOrangePhase2 = sourceComponent.outOrangePhase2
            targetComponent.inApplePhase1 = dynamicComponent.outApplePhase1
            targetComponent.inOrangePhase1 = dynamicComponent.outOrangePhase1
            targetComponent.inOrangePhase2 = dynamicComponent.outOrangePhase2
        }
        dynamicComponent.optimizeWiring()
        dynamicComponent.allocateChannelsToPhases()

        TestPacketOrange orangeA = new TestPacketOrange()
        TestPacketOrange orangeB = new TestPacketOrange()
        TestPacketOrange orangeC = new TestPacketOrange()
        TestPacketOrange orangeD = new TestPacketOrange()
        TestPacketApple appleA = new TestPacketApple()
        TestPacketApple appleB = new TestPacketApple()

        sourceComponent.inApplePhase1 << appleA << appleB
        sourceComponent.inOrangePhase1 << orangeA << orangeB << orangeC
        sourceComponent.inOrangePhase2 << orangeD

        TestProbe probe = new TestProbe(sub2, 'outApplePhase1')
        List dynamicComponentApplePhase1 = probe.result
        List dynamicComponentOrangePhase1 = new TestProbe(sub2, 'outOrangePhase1').result
        List dynamicComponentOrangePhase2 = new TestProbe(sub2, 'outOrangePhase2').result
        List targetComponentApplePhase1 = new TestProbe(targetComponent, 'outApplePhase1').result
        List targetComponentOrangePhase1 = new TestProbe(targetComponent, 'outOrangePhase1').result
        List targetComponentOrangePhase2 = new TestProbe(targetComponent, 'outOrangePhase2').result

        sourceComponent.calculateAndPublish("Phase 1")
        assertEquals 'dynamicComponent.outApplePhase1.size', 2,  dynamicComponentApplePhase1.size()
        assertEquals 'dynamicComponent.outOrangePhase1.size', 3, dynamicComponentOrangePhase1.size()
        assertEquals 'dynamicComponent.outOrangePhase2.size', 0, dynamicComponentOrangePhase2.size()

        assertEquals 'targetComponent.outApplePhase1.size', 4, targetComponentApplePhase1.size()
        assertEquals 'targetComponent.outOrangePhase1.size', 6, targetComponentOrangePhase1.size()
        assertEquals 'targetComponent.outOrangePhase2.size', 0, targetComponentOrangePhase2.size()

        sourceComponent.calculateAndPublish("Phase 2")
        assertEquals 'dynamicComponent.outApplePhase1.size', 2,  dynamicComponentApplePhase1.size()
        assertEquals 'dynamicComponent.outOrangePhase1.size', 3, dynamicComponentOrangePhase1.size()
        assertEquals 'dynamicComponent.outOrangePhase2.size', 1, dynamicComponentOrangePhase2.size()

        assertEquals 'targetComponent.outApplePhase1.size', 4, targetComponentApplePhase1.size()
        assertEquals 'targetComponent.outOrangePhase1.size', 6, targetComponentOrangePhase1.size()
        assertEquals 'targetComponent.outOrangePhase2.size', 2, targetComponentOrangePhase2.size()
    }

    // https://issuetracking.intuitive-collaboration.com/jira/browse/PMO-1733
    void testMultiInChannels() {
        dynamicComponent.addSubComponent sub1
        dynamicComponent.addSubComponent sub2
        dynamicComponent.internalWiring()
        WiringUtils.use(WireCategory) {
            dynamicComponent.inApplePhase1 = sourceComponent.outApplePhase1
            dynamicComponent.inOrangePhase1 = sourceComponent.outOrangePhase1
            dynamicComponent.inOrangePhase2 = sourceComponent.outOrangePhase2
            dynamicComponent.inApplePhase1 = sourceComponent2.outApplePhase1
            dynamicComponent.inOrangePhase1 = sourceComponent2.outOrangePhase1
            dynamicComponent.inOrangePhase2 = sourceComponent2.outOrangePhase2
            targetComponent.inApplePhase1 = dynamicComponent.outApplePhase1
            targetComponent.inOrangePhase1 = dynamicComponent.outOrangePhase1
            targetComponent.inOrangePhase2 = dynamicComponent.outOrangePhase2
        }
        dynamicComponent.optimizeWiring()
        dynamicComponent.allocateChannelsToPhases()

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

        List dynamicComponentApplePhase1 = new TestProbe(sub2, 'outApplePhase1').result
        List dynamicComponentOrangePhase1 = new TestProbe(sub2, 'outOrangePhase1').result
        List dynamicComponentOrangePhase2 = new TestProbe(sub2, 'outOrangePhase2').result
        List targetComponentApplePhase1 = new TestProbe(targetComponent, 'outApplePhase1').result
        List targetComponentOrangePhase1 = new TestProbe(targetComponent, 'outOrangePhase1').result
        List targetComponentOrangePhase2 = new TestProbe(targetComponent, 'outOrangePhase2').result

        sourceComponent.calculateAndPublish("Phase 1")
        sourceComponent2.calculateAndPublish("Phase 1")
        assertEquals 'dynamicComponent.outApplePhase1.size', 4,  dynamicComponentApplePhase1.size()
        assertEquals 'dynamicComponent.outOrangePhase1.size', 6, dynamicComponentOrangePhase1.size()
        assertEquals 'dynamicComponent.outOrangePhase2.size', 0, dynamicComponentOrangePhase2.size()

        assertEquals 'targetComponent.outApplePhase1.size', 8, targetComponentApplePhase1.size()
        assertEquals 'targetComponent.outOrangePhase1.size', 12, targetComponentOrangePhase1.size()
        assertEquals 'targetComponent.outOrangePhase2.size', 0, targetComponentOrangePhase2.size()

        sourceComponent.calculateAndPublish("Phase 2")
        sourceComponent2.calculateAndPublish("Phase 2")
        assertEquals 'dynamicComponent.outApplePhase1.size', 4,  dynamicComponentApplePhase1.size()
        assertEquals 'dynamicComponent.outOrangePhase1.size', 6, dynamicComponentOrangePhase1.size()
        assertEquals 'dynamicComponent.outOrangePhase2.size', 2, dynamicComponentOrangePhase2.size()

        assertEquals 'targetComponent.outApplePhase1.size', 8, targetComponentApplePhase1.size()
        assertEquals 'targetComponent.outOrangePhase1.size', 12, targetComponentOrangePhase1.size()
        assertEquals 'targetComponent.outOrangePhase2.size', 4, targetComponentOrangePhase2.size()
    }
}
