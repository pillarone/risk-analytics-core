package org.pillarone.riskanalytics.core.components

import org.pillarone.riskanalytics.core.example.component.TestComponentWithPeriodStore
import org.pillarone.riskanalytics.core.packets.SingleValuePacket
import org.pillarone.riskanalytics.core.simulation.engine.PeriodScope
import org.pillarone.riskanalytics.core.example.component.TestComponentWithIterationStore
import org.pillarone.riskanalytics.core.simulation.engine.IterationScope

/**
 * @author dierk.koenig (at) canoo (dot) com, stefan.kunz (at) intuitive-collaboration (dot) com
 */
class IterationStoreTests extends GroovyTestCase {

    TestComponentWithIterationStore component

    void setUp() {
        component = new TestComponentWithIterationStore()
        component.iterationScope = new IterationScope(periodScope: new PeriodScope())
        component.iterationScope.prepareNextIteration()
        component.iterationStore = new IterationStore(component.iterationScope)
    }

    protected void tearDown() {
        component = null
    }

    void testInitial() {
        component.iterationScope.periodScope.currentPeriod = 0
        component.doCalculation() // 1st period
        assertEquals "correct value 5", 5d, component.outPacket[0].value
        component.outPacket.clear()

        component.iterationScope.periodScope.currentPeriod = 1
        component.doCalculation() // 2nd period
        assertEquals "correct value 5", 5d, component.outPacket[0].value
    }

    // fail if user tries to write to history -> IAE
    void testBlockWriteToHistory() {
        component.iterationScope.periodScope.currentPeriod = 0

        shouldFail(IllegalArgumentException, {
            component.iterationStore.put(TestComponentWithPeriodStore.PAID, null, PeriodStore.LAST_PERIOD)
        })
    }

    void testBlockWriteAfterFirstIteration() {
        component.iterationScope.prepareNextIteration()

        shouldFail(IllegalArgumentException, {
            component.iterationStore.put(TestComponentWithPeriodStore.PAID, null)
        })
    }

    // fail if an unknown key is used -> return null
    void testKeyNotAvailable() {
        component.iterationScope.periodScope.currentPeriod = 0
        component.doCalculation() // 1st period
        assertNull "reserved key not available", component.iterationStore.get("reserved", PeriodStore.CURRENT_PERIOD)
    }

    // fail when outside bounds, e.g. currentPeriod == 0 , periodStore[-1]
    void testCheckBounds() {
        component.iterationScope.periodScope.currentPeriod = 0
        component.doCalculation() // 1st period

        shouldFail(IllegalArgumentException, {
            component.iterationStore.get(TestComponentWithPeriodStore.PAID, PeriodStore.LAST_PERIOD)
        })
    }

    void testPut() {
        component.iterationScope.periodScope.currentPeriod = 0
        SingleValuePacket packetPeriod0 = new SingleValuePacket(value: 1)
        component.iterationStore.put(TestComponentWithPeriodStore.PAID, packetPeriod0, PeriodStore.CURRENT_PERIOD)
        assertEquals "period 0, get with idx", packetPeriod0, component.iterationStore.get(TestComponentWithPeriodStore.PAID, PeriodStore.CURRENT_PERIOD)
        assertEquals "period 0", packetPeriod0, component.iterationStore.get(TestComponentWithPeriodStore.PAID)
    }

    void testPutAll() {
        component.iterationScope.periodScope.currentPeriod = 0
        List<SingleValuePacket> packets = []
        SingleValuePacket packetPeriod0 = new SingleValuePacket(value: 1)
        SingleValuePacket packetPeriod1 = new SingleValuePacket(value: 2)
        packets << packetPeriod0 << packetPeriod1
        component.iterationStore.putAll(TestComponentWithPeriodStore.PAID, packets, PeriodStore.CURRENT_PERIOD)
        assertEquals "period 0", packetPeriod0, component.iterationStore.get(TestComponentWithPeriodStore.PAID, PeriodStore.CURRENT_PERIOD)
        assertEquals "period 1", packetPeriod1, component.iterationStore.get(TestComponentWithPeriodStore.PAID, 1)
    }

    void testGetList() {
        component.iterationScope.periodScope.currentPeriod = 0
        List<SingleValuePacket> packets = []
        SingleValuePacket packetPeriod0 = new SingleValuePacket(value: 1)
        SingleValuePacket packetPeriod1 = new SingleValuePacket(value: 2)
        packets << packetPeriod0 << packetPeriod1
        component.iterationStore.putAll(TestComponentWithPeriodStore.PAID, packets, PeriodStore.CURRENT_PERIOD)
        assertEquals "period 0", [packetPeriod0], component.iterationStore.get(TestComponentWithPeriodStore.PAID, PeriodStore.CURRENT_PERIOD, PeriodStore.CURRENT_PERIOD)
        assertEquals "all range", [packetPeriod0, packetPeriod1], component.iterationStore.get(TestComponentWithPeriodStore.PAID, 0, 1)
        assertEquals "all", [packetPeriod0, packetPeriod1], component.iterationStore.getAll(TestComponentWithPeriodStore.PAID)
    }
}