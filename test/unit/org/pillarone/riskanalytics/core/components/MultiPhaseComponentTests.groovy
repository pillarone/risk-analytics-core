package org.pillarone.riskanalytics.core.components

import org.pillarone.riskanalytics.core.packets.TestPacketOrange
import org.pillarone.riskanalytics.core.packets.TestPacketApple
import org.pillarone.riskanalytics.core.util.TestProbe
import org.pillarone.riskanalytics.core.wiring.WireCategory
import org.pillarone.riskanalytics.core.wiring.WiringUtils

/**
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
class MultiPhaseComponentTests extends GroovyTestCase {

    TestMultiPhaseComponent component1 = new TestMultiPhaseComponent(name: 'component1')
    TestMultiPhaseComponent component2 = new TestMultiPhaseComponent(name: 'component2')

    void testUsage() {
        TestMultiPhaseComponent component = new TestMultiPhaseComponent()
        component.allocateChannelsToPhases()
        TestPacketOrange orangeA = new TestPacketOrange()
        TestPacketOrange orangeB = new TestPacketOrange()
        TestPacketOrange orangeC = new TestPacketOrange()
        TestPacketOrange orangeD = new TestPacketOrange()
        TestPacketApple appleA = new TestPacketApple()
        TestPacketApple appleB = new TestPacketApple()

        component.inApplePhase1 << appleA << appleB
        component.inOrangePhase1 << orangeA << orangeB << orangeC
        component.inOrangePhase2 << orangeD

        component.calculateAndPublish("Phase 1")
        assertEquals 'outApplePhase1 channel void', 2, component.outApplePhase1.size()
        assertEquals 'outOrangePhase1 channel void', 3, component.outOrangePhase1.size()
        assertEquals 'outOrangePhase2 channel void', 0, component.outOrangePhase2.size()        
        assertEquals 'appleA', appleA, component.outApplePhase1[0]
        assertEquals 'appleB', appleB, component.outApplePhase1[1]
        assertEquals 'orangeA', orangeA, component.outOrangePhase1[0]
        assertEquals 'orangeB', orangeB, component.outOrangePhase1[1]
        assertEquals 'orangeC', orangeC, component.outOrangePhase1[2]

        component.calculateAndPublish("Phase 2")
        assertEquals 'outApplePhase1 channel void', 2, component.outApplePhase1.size()
        assertEquals 'outOrangePhase1 channel void', 3, component.outOrangePhase1.size()
        assertEquals 'outOrangePhase2 channel void', 1, component.outOrangePhase2.size()
        assertEquals 'appleA', appleA, component.outApplePhase1[0]
        assertEquals 'appleB', appleB, component.outApplePhase1[1]
        assertEquals 'orangeA', orangeA, component.outOrangePhase1[0]
        assertEquals 'orangeB', orangeB, component.outOrangePhase1[1]
        assertEquals 'orangeC', orangeC, component.outOrangePhase1[2]
        assertEquals 'orangeD', orangeD, component.outOrangePhase2[0]

        component.reset()
        assertEquals 'outApplePhase1 channel void', 0, component.outApplePhase1.size()
        assertEquals 'outOrangePhase1 channel void', 0, component.outOrangePhase1.size()
        assertEquals 'outOrangePhase2 channel void', 0, component.outOrangePhase2.size()
    }

    void testSeveralComponents() {
        TestPacketOrange orangeA = new TestPacketOrange()
        TestPacketOrange orangeB = new TestPacketOrange()
        TestPacketOrange orangeC = new TestPacketOrange()
        TestPacketOrange orangeD = new TestPacketOrange()
        TestPacketApple appleA = new TestPacketApple()
        TestPacketApple appleB = new TestPacketApple()

        WiringUtils.use(WireCategory) {
            component2.inApplePhase1 = component1.outApplePhase1
            component2.inOrangePhase1 = component1.outOrangePhase1
            component2.inOrangePhase2 = component1.outOrangePhase2
        }

        component1.inApplePhase1 << appleA << appleB
        component1.inOrangePhase1 << orangeA << orangeB << orangeC
        component1.inOrangePhase2 << orangeD

        List component1ApplePhase1 = new TestProbe(component1, 'outApplePhase1').result
        List component1OrangePhase1 = new TestProbe(component1, 'outOrangePhase1').result
        List component1OrangePhase2 = new TestProbe(component1, 'outOrangePhase2').result
        List component2ApplePhase1 = new TestProbe(component2, 'outApplePhase1').result
        List component2OrangePhase1 = new TestProbe(component2, 'outOrangePhase1').result
        List component2OrangePhase2 = new TestProbe(component2, 'outOrangePhase2').result

        component1.calculateAndPublish("Phase 1")
        // no reset before not all phases have been executed
        assertEquals 'component1.outApplePhase1 channel void', 2, component1.outApplePhase1.size()
        assertEquals 'component1.outOrangePhase1 channel void', 3, component1.outOrangePhase1.size()
        assertEquals 'component1.outOrangePhase2 channel void', 0, component1.outOrangePhase2.size()
        assertEquals 'component2.outApplePhase1 channel void', 2, component2.outApplePhase1.size()
        assertEquals 'component2.outOrangePhase1 channel void', 3, component2.outOrangePhase1.size()
        assertEquals 'component2.outOrangePhase2 channel void', 0, component2.outOrangePhase2.size()

        assertEquals 'component1.outApplePhase1 channel void', 2, component1ApplePhase1.size()
        assertEquals 'component1.outOrangePhase1 channel void', 3, component1OrangePhase1.size()
        assertEquals 'component1.outOrangePhase2 channel void', 0, component1OrangePhase2.size()
        assertEquals 'component2.outApplePhase1 channel void', 2, component2ApplePhase1.size()
        assertEquals 'component2.outOrangePhase1 channel void', 3, component2OrangePhase1.size()
        assertEquals 'component2.outOrangePhase2 channel void', 0, component2OrangePhase2.size()
        assertEquals 'component1.appleA', appleA, component1ApplePhase1[0]
        assertEquals 'component1.appleB', appleB, component1ApplePhase1[1]
        assertEquals 'component1.orangeA', orangeA, component1OrangePhase1[0]
        assertEquals 'component1.orangeB', orangeB, component1OrangePhase1[1]
        assertEquals 'component1.orangeC', orangeC, component1OrangePhase1[2]
        assertEquals 'component2.appleA', appleA, component2ApplePhase1[0]
        assertEquals 'component2.appleB', appleB, component2ApplePhase1[1]
        assertEquals 'component2.orangeA', orangeA, component2OrangePhase1[0]
        assertEquals 'component2.orangeB', orangeB, component2OrangePhase1[1]
        assertEquals 'component2.orangeC', orangeC, component2OrangePhase1[2]


        component1.calculateAndPublish("Phase 2")
        assertEquals 'component1.outApplePhase1 channel void', 0, component1.outApplePhase1.size()
        assertEquals 'component1.outOrangePhase1 channel void', 0, component1.outOrangePhase1.size()
        assertEquals 'component1.outOrangePhase2 channel void', 0, component1.outOrangePhase2.size()
        assertEquals 'component2.outApplePhase1 channel void', 0, component2.outApplePhase1.size()
        assertEquals 'component2.outOrangePhase1 channel void', 0, component2.outOrangePhase1.size()
        assertEquals 'component2.outOrangePhase2 channel void', 0, component2.outOrangePhase2.size()
        assertEquals 'component1.outApplePhase1 channel void', 2, component1ApplePhase1.size()
        assertEquals 'component1.outOrangePhase1 channel void', 3, component1OrangePhase1.size()
        assertEquals 'component1.outOrangePhase2 channel void', 1, component1OrangePhase2.size()
        assertEquals 'component2.outApplePhase1 channel void', 2, component2ApplePhase1.size()
        assertEquals 'component2.outOrangePhase1 channel void', 3, component2OrangePhase1.size()
        assertEquals 'component2.outOrangePhase2 channel void', 1, component2OrangePhase2.size()
        assertEquals 'component1.appleA', appleA, component1ApplePhase1[0]
        assertEquals 'component1.appleB', appleB, component1ApplePhase1[1]
        assertEquals 'component1.orangeA', orangeA, component1OrangePhase1[0]
        assertEquals 'component1.orangeB', orangeB, component1OrangePhase1[1]
        assertEquals 'component1.orangeC', orangeC, component1OrangePhase1[2]
        assertEquals 'component1.orangeD', orangeD, component1OrangePhase2[0]
        assertEquals 'component2.appleA', appleA, component2ApplePhase1[0]
        assertEquals 'component2.appleB', appleB, component2ApplePhase1[1]
        assertEquals 'component2.orangeA', orangeA, component2OrangePhase1[0]
        assertEquals 'component2.orangeB', orangeB, component2OrangePhase1[1]
        assertEquals 'component2.orangeC', orangeC, component2OrangePhase1[2]
        assertEquals 'component2.orangeD', orangeD, component2OrangePhase2[0]
    }

    void testExecute() {
        TestPacketOrange orangeA = new TestPacketOrange()
        TestPacketOrange orangeB = new TestPacketOrange()
        TestPacketOrange orangeC = new TestPacketOrange()
        TestPacketOrange orangeD = new TestPacketOrange()
        TestPacketApple appleA = new TestPacketApple()
        TestPacketApple appleB = new TestPacketApple()

        WiringUtils.use(WireCategory) {
            component2.inApplePhase1 = component1.outApplePhase1
            component2.inOrangePhase1 = component1.outOrangePhase1
            component2.inOrangePhase2 = component1.outOrangePhase2
        }

        component1.inApplePhase1 << appleA << appleB
        component1.inOrangePhase1 << orangeA << orangeB << orangeC
        component1.inOrangePhase2 << orangeD

        List component1ApplePhase1 = new TestProbe(component1, 'outApplePhase1').result
        List component1OrangePhase1 = new TestProbe(component1, 'outOrangePhase1').result
        List component1OrangePhase2 = new TestProbe(component1, 'outOrangePhase2').result
        List component2ApplePhase1 = new TestProbe(component2, 'outApplePhase1').result
        List component2OrangePhase1 = new TestProbe(component2, 'outOrangePhase1').result
        List component2OrangePhase2 = new TestProbe(component2, 'outOrangePhase2').result

        component1.execute()
        assertEquals 'component1.outApplePhase1 channel void', 0, component1.outApplePhase1.size()
        assertEquals 'component1.outOrangePhase1 channel void', 0, component1.outOrangePhase1.size()
        assertEquals 'component1.outOrangePhase2 channel void', 0, component1.outOrangePhase2.size()
        assertEquals 'component2.outApplePhase1 channel void', 0, component2.outApplePhase1.size()
        assertEquals 'component2.outOrangePhase1 channel void', 0, component2.outOrangePhase1.size()
        assertEquals 'component2.outOrangePhase2 channel void', 0, component2.outOrangePhase2.size()
        assertEquals 'component1.outApplePhase1 channel void', 0, component1.outApplePhase1.size()
        assertEquals 'component1.outOrangePhase1 channel void', 0, component1.outOrangePhase1.size()
        assertEquals 'component1.outOrangePhase2 channel void', 0, component1.outOrangePhase2.size()
        assertEquals 'component2.outApplePhase1 channel void', 0, component2.outApplePhase1.size()
        assertEquals 'component2.outOrangePhase1 channel void', 0, component2.outOrangePhase1.size()
        assertEquals 'component2.outOrangePhase2 channel void', 0, component2.outOrangePhase2.size()
        assertEquals 'component1.outApplePhase1 channel void', 2, component1ApplePhase1.size()
        assertEquals 'component1.outOrangePhase1 channel void', 3, component1OrangePhase1.size()
        assertEquals 'component1.outOrangePhase2 channel void', 1, component1OrangePhase2.size()
        assertEquals 'component2.outApplePhase1 channel void', 2, component2ApplePhase1.size()
        assertEquals 'component2.outOrangePhase1 channel void', 3, component2OrangePhase1.size()
        assertEquals 'component2.outOrangePhase2 channel void', 1, component2OrangePhase2.size()
        assertEquals 'component1.appleA', appleA, component1ApplePhase1[0]
        assertEquals 'component1.appleB', appleB, component1ApplePhase1[1]
        assertEquals 'component1.orangeA', orangeA, component1OrangePhase1[0]
        assertEquals 'component1.orangeB', orangeB, component1OrangePhase1[1]
        assertEquals 'component1.orangeC', orangeC, component1OrangePhase1[2]
        assertEquals 'component1.orangeD', orangeD, component1OrangePhase2[0]
        assertEquals 'component2.appleA', appleA, component2ApplePhase1[0]
        assertEquals 'component2.appleB', appleB, component2ApplePhase1[1]
        assertEquals 'component2.orangeA', orangeA, component2OrangePhase1[0]
        assertEquals 'component2.orangeB', orangeB, component2OrangePhase1[1]
        assertEquals 'component2.orangeC', orangeC, component2OrangePhase1[2]
        assertEquals 'component2.orangeD', orangeD, component2OrangePhase2[0]
    }
}
