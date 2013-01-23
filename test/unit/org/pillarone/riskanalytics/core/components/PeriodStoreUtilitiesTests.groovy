package org.pillarone.riskanalytics.core.components

import org.pillarone.riskanalytics.core.simulation.engine.PeriodScope

/**
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
class PeriodStoreUtilitiesTests extends GroovyTestCase {

    PeriodStoreUtilities utilities = new PeriodStoreUtilities()
    PeriodStore store
    public static final String PAID = "paid"

    void setUp() {
        store = new PeriodStore(new PeriodScope())
        store.periodScope.currentPeriod = 0
        store.putAll PAID, [1, 2, 3, 4, 5], 0
    }

    void tearDown() {
        store.clear()
    }

    void testGetSumOfFutureObjects() {
        assertEquals "all periods", 15, utilities.getSumOfFutureObjects(store, PAID, true)
        assertEquals "all periods", 14, utilities.getSumOfFutureObjects(store, PAID, false)

        store.periodScope.currentPeriod = 2
        assertEquals "all periods", 12, utilities.getSumOfFutureObjects(store, PAID)
        assertEquals "all periods", 9, utilities.getSumOfFutureObjects(store, PAID, false)
    }

    void testGetSum() {
        assertEquals "all periods", 15, utilities.getSum(store, PAID)
        assertEquals "all periods", 15, utilities.getSum(store, PAID, 0)
        assertEquals "all periods", 9, utilities.getSum(store, PAID, 3)
        assertEquals "all periods", 7, utilities.getSum(store, PAID, 2, 3)
    }
}