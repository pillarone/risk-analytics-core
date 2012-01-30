package org.pillarone.riskanalytics.core.components

import org.pillarone.riskanalytics.core.packets.Packet
import org.pillarone.riskanalytics.core.wiring.WireCategory
import org.pillarone.riskanalytics.core.wiring.WiringUtils
import org.pillarone.riskanalytics.core.example.component.TestComponent
import org.pillarone.riskanalytics.core.example.component.TestComposedComponent
import org.pillarone.riskanalytics.core.util.TestProbe
import org.pillarone.riskanalytics.core.packets.SingleValuePacket


class ComposedComponentTests extends GroovyTestCase {

    TestComposedComponent component
    TestComponent successor

    void testExecute() {
        component = new TestComposedComponent()
        component.internalWiring()

        def probeSub1Value1 = new TestProbe(component.subComponent1, "outValue1")
        List sub1Value1 = probeSub1Value1.result

        def probeSub2Value1 = new TestProbe(component.subComponent2, "outValue1")
        List sub2Value1 = probeSub2Value1.result

        def probeValue1 = new TestProbe(component, "outValue1")
        List value1 = probeValue1.result
        def probeValue2 = new TestProbe(component, "outValue2")
        List value2 = probeValue2.result

        Packet packet1 = new Packet()
        Packet packet2 = new Packet()
        component.input1 << packet1
        component.input2 << packet2

        assertTrue("outValue1 was empty", value1.isEmpty())
        assertTrue("outValue2 was empty", value2.isEmpty())

        component.start()

        assertSame("input1 and output1 have to be the same", packet1, value1[0])
        assertSame("input1 and output1 have to be the same in sub component 1", packet1, sub1Value1[0])
        assertSame("input1 and output1 have to be the same in sub component 2", packet1, sub2Value1[0])
        assertSame("input2 and output2 have to be the same", packet2, value2[0])
    }

    void testReset() {
        component = new TestComposedComponent()
        component.internalWiring()

        component.input1 << new Packet()
        component.input2 << new Packet()
        component.outValue1 << new Packet()
        component.outValue2 << new Packet()

        component.subComponent1.input1 << new Packet()
        component.subComponent1.input2 << new Packet()
        component.subComponent1.outValue1 << new Packet()
        component.subComponent1.outValue2 << new Packet()

        component.subComponent2.input1 << new Packet()
        component.subComponent2.input2 << new Packet()
        component.subComponent2.outValue1 << new Packet()
        component.subComponent2.outValue2 << new Packet()

        assertFalse("component.input1 empty", component.input1.isEmpty())
        assertFalse("component.input2 empty", component.input2.isEmpty())
        assertFalse("component.outValue1 empty", component.outValue1.isEmpty())
        assertFalse("component.outValue2 empty", component.outValue2.isEmpty())

        component.reset()

        assertTrue("component.input1 not empty", component.input1.isEmpty())
        assertTrue("component.input2 not empty", component.input2.isEmpty())
        assertTrue("component.outValue1 not empty", component.outValue1.isEmpty())
        assertTrue("component.outValue2 not empty", component.outValue2.isEmpty())
    }


    void testOptimizeWiring() {
        component = new TestComposedComponent()
        successor = new TestComponent()
        component.internalWiring()

        WiringUtils.use(WireCategory) {
            successor.input1 = component.outValue1
        }

        assertEquals 2, component.allOutputReplicationTransmitter.size()

        component.optimizeWiring()

        assertEquals 1, component.allOutputReplicationTransmitter.size()
        assertEquals 1, component.subComponent2.allOutputTransmitter.size()

        assertSame component.subComponent2, component.allOutputReplicationTransmitter[0].sender
        assertSame component.outValue1, component.allOutputReplicationTransmitter[0].target
    }

    void testInnerStartComponentResolution() {
        TestStartComposedComponent component = new TestStartComposedComponent()
        component.internalWiring()
        component.input1 << new SingleValuePacket(value:  1.5)
        component.doCalculation()

        assertTrue "component A", component.subComponentA.doCalculationCalled
        assertTrue "component B", component.subComponentB.doCalculationCalled
        assertTrue "component C", component.subComponentC.doCalculationCalled

        assertEquals "component A (outValue1)", [3d], component.outValue1*.value
        assertEquals "component B,C (outValue2)", [4d], component.outValue2*.value
    }

    void testInnerStartComponentResolution2() {
        TestStartNestedComposedComponent component = new TestStartNestedComposedComponent(name: 'outest')
        component.internalWiring()
        component.doCalculation()

        assertTrue "component a", component.subComponentA.doCalculationCalled
        assertTrue "component b", component.subComponentB.doCalculationCalled
        assertTrue "component c.A", component.subComponentC.subComponentA.doCalculationCalled
        assertTrue "component c.B", component.subComponentC.subComponentB.doCalculationCalled
        assertTrue "component c.C", component.subComponentC.subComponentC.doCalculationCalled

        assertEquals "outValue 1", [2d], component.outValue1*.value
        assertEquals "outValue 2", [12d], component.outValue2*.value

        component.reset()

        component.subComponentC.subComponentC.parmValue = 3
        component.doCalculation()
        assertEquals "outValue 1", [2d], component.outValue1*.value
        assertEquals "outValue 2", [16d], component.outValue2*.value

    }
}

