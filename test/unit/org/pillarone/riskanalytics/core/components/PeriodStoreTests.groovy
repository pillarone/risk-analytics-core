package org.pillarone.riskanalytics.core.components

import org.pillarone.riskanalytics.core.packets.SingleValuePacket
import org.pillarone.riskanalytics.core.simulation.engine.PeriodScope
import org.pillarone.riskanalytics.core.example.component.TestComponentWithPeriodStore

/**
 * @author dierk.koenig (at) canoo (dot) com, stefan.kunz (at) intuitive-collaboration (dot) com
 */
class PeriodStoreTests extends GroovyTestCase {

    TestComponentWithPeriodStore component

    void setUp() {
        component = new TestComponentWithPeriodStore()
        component.periodScope = new PeriodScope(currentPeriod : 0)
        component.periodStore = new PeriodStore(component.periodScope)
    }

    protected void tearDown() {
        component = null
    }    

    void testInitial() {
        component.doCalculation() // 1st period
        assertEquals "correct value 5", 5d, component.outPacket[0].value
        component.outPacket.clear()

        component.periodScope.currentPeriod = 1
        component.doCalculation() // 2nd period
        assertEquals "correct value 5", 5d, component.outPacket[0].value
    }

    // fail if user tries to write to history -> IAE
    void testBlockWriteToHistory() {
        shouldFail(IllegalArgumentException, {
            component.periodStore.put(TestComponentWithPeriodStore.PAID, null, PeriodStore.LAST_PERIOD)
        })
    }

    // fail if an unknown key is used -> return null
    void testKeyNotAvailable() {
        component.doCalculation() // 1st period
        assertNull "reserved key not available", component.periodStore.get("reserved", PeriodStore.CURRENT_PERIOD)
    }

    // fail when outside bounds, e.g. currentPeriod == 0 , periodStore[-1]
    void testCheckBounds() {
        component.doCalculation() // 1st period

        shouldFail(IllegalArgumentException, {
            component.periodStore.get(TestComponentWithPeriodStore.PAID, PeriodStore.LAST_PERIOD)
        })
    }

    void testPut() {
        SingleValuePacket packetPeriod0 = new SingleValuePacket(value: 1)
        component.periodStore.put(TestComponentWithPeriodStore.PAID, packetPeriod0, PeriodStore.CURRENT_PERIOD)
        assertEquals "period 0, get with idx", packetPeriod0, component.periodStore.get(TestComponentWithPeriodStore.PAID, PeriodStore.CURRENT_PERIOD)
        assertEquals "period 0", packetPeriod0, component.periodStore.get(TestComponentWithPeriodStore.PAID)
    }

    void testClear() {
        SingleValuePacket packetPeriod0 = new SingleValuePacket(value: 1)
        component.periodStore.put(TestComponentWithPeriodStore.PAID, packetPeriod0, PeriodStore.CURRENT_PERIOD)

        assertFalse "packet persisted", component.periodStore.empty()
        component.periodStore.clear()
        assertTrue "all packets removed", component.periodStore.empty()
    }

    void testPutAll() {
        List<SingleValuePacket> packets = []
        SingleValuePacket packetPeriod0 = new SingleValuePacket(value: 1)
        SingleValuePacket packetPeriod1 = new SingleValuePacket(value: 2)
        packets << packetPeriod0 << packetPeriod1
        component.periodStore.putAll(TestComponentWithPeriodStore.PAID, packets, PeriodStore.CURRENT_PERIOD)
        assertEquals "period 0", packetPeriod0, component.periodStore.get(TestComponentWithPeriodStore.PAID, PeriodStore.CURRENT_PERIOD)
        assertEquals "period 1", packetPeriod1, component.periodStore.get(TestComponentWithPeriodStore.PAID, 1)
    }

    void testGetList() {
        List<SingleValuePacket> packets = []
        SingleValuePacket packetPeriod0 = new SingleValuePacket(value: 1)
        SingleValuePacket packetPeriod1 = new SingleValuePacket(value: 2)
        packets << packetPeriod0 << packetPeriod1
        component.periodStore.putAll(TestComponentWithPeriodStore.PAID, packets, PeriodStore.CURRENT_PERIOD)
        assertEquals "period 0", [packetPeriod0], component.periodStore.get(TestComponentWithPeriodStore.PAID, PeriodStore.CURRENT_PERIOD, PeriodStore.CURRENT_PERIOD)
        assertEquals "all range", [packetPeriod0, packetPeriod1], component.periodStore.get(TestComponentWithPeriodStore.PAID, 0, 1)
        assertEquals "all", [packetPeriod0, packetPeriod1], component.periodStore.getAll(TestComponentWithPeriodStore.PAID)
    }
}

