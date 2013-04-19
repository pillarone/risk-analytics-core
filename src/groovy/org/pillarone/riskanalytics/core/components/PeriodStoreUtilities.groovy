package org.pillarone.riskanalytics.core.components

import groovy.transform.CompileStatic

/**
 *  This class provides convenience functions for a period store:<br>
 *  <ul><li>sum calculation</li></ul>
 *
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
@CompileStatic
public class PeriodStoreUtilities {

    static Object getSumOfFutureObjects(PeriodStore store, String key, boolean includeCurrentPeriodObject = true) {
        int period = store.periodScope.currentPeriod
        return getSum(store, key, includeCurrentPeriodObject ? period : period + 1)
    }

    static Object getSum(PeriodStore store, String key) {
        return getSum(store, key, -store.periodScope.currentPeriod)
    }

    static Object getSum(PeriodStore store, String key, int fromPeriodOffset) {
        if (store.get(key) instanceof Number) {
            List objects = store.getAll(key, fromPeriodOffset)
            return objects.sum()
        }
        else {
            throw new IllegalArgumentException("PeriodStore objects for key ${key} are not numbers.")
        }
    }

    static Object getSum(PeriodStore store, String key, int fromPeriod, int toPeriod) {
        if (store.get(key) instanceof Number) {
            List objects = store.get(key, fromPeriod, toPeriod)
            return objects.sum()
        }
        else {
            throw new IllegalArgumentException("PeriodStore objects for key ${key} are not numbers.")
        }
    }
}
