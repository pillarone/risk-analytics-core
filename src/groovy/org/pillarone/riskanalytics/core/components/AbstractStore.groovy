package org.pillarone.riskanalytics.core.components

import groovy.transform.TypeChecked

import java.util.*
import org.pillarone.riskanalytics.core.simulation.engine.PeriodScope;

/**
 *  A PeriodStore is used whenever a Component needs a memory in order to access data from former periods or
 *  prepare data that the Component itself will need in future periods. In order to enable this memory
 *  capability a component needs one member variable:
 *  <code>
 *  PeriodStore periodStore
 *  </code>
 *  Both are initialized by injection.
 *
 *  The PeriodStore may keep different kind of data, as it accepts objects addressed by keys.
 *
 *  @author dierk.koenig (at) canoo (dot) com, stefan.kunz (at) intuitive-collaboration (dot) com
 */
@TypeChecked
abstract public class AbstractStore implements Serializable{

    public static int CURRENT_PERIOD = 0
    public static int LAST_PERIOD = -1

    protected PeriodScope periodScope

    private Map<String, List<Object>> store = new HashMap<String, List<Object>>()

    abstract public void initPeriodScope(PeriodScope periodScope)

    /**
     *  Stores the object for the current period (calls put(key, s, 0)).
     *  @param key
     *  @param s
     */
    public void put(String key, Object s) {
        put(key, s, 0)
    }

    /**
     *  Stores the object into current period + periodOffset. Writing to past period is not permitted. Therefore
     *  periodOffset has to be 0 or positive.
     *  @param key
     *  @param s
     *  @param periodOffset
     *  @param insertPeriod
     */
    public void put(String key, Object s, int periodOffset) {
        if (periodOffset < 0) {
            throw new IllegalArgumentException("No write access to history of PeriodStore (periodOffset=${periodOffset})")
        }
        List list = store.get(key, [])
        int insertPeriod = periodScope.currentPeriod + periodOffset
        while (list.size() <= insertPeriod) {
            list.add(null);
        }
        list.set(insertPeriod, s)
    }

    /**
     *  Stores the elements of the list starting in current period + first object belongs to period offset
     *  @param key
     *  @param list
     *  @param firstObjectBelongsToPeriodOffset
     */
    public void putAll(String key, List list, int firstObjectBelongsToPeriodOffset) {
        for (Object o: list) {
            put(key, o, firstObjectBelongsToPeriodOffset++)
        }
    }

    /**
     * @param key
     * @return object belonging to key of the current period (calls get(key, 0))
     */
    public Object get(String key) {
        return get(key, 0)
    }

    /**
     * @param key
     * @return object belonging to key of the current period (calls get(key, 0))
     */
    public Object getFirstPeriod(String key) {
        return get(key, -periodScope.getCurrentPeriod())
    }

    /**
     *  If periodOffset is larger than current period an IllegalArgumentException is thrown as the PeriodStore
     *  can't keep object objects for periods before the simulation start period.
     *
     *  @param key
     *  @param periodOffset
     *  @return object belonging to key belonging to current period + period offset
     */
    public Object get(String key, int periodOffset) {
        if (store.get(key) != null) {
            int period = periodScope.currentPeriod + periodOffset
            if (period < 0) {
                throw new IllegalArgumentException("No access for periods before the simulation start period (period=${period}).")
            }
            if (period >= store.get(key).size()) {
                return null
            }
            return store.get(key).get(period)
        }
        else {
            return null
        }
    }

    public Object getCloned(String key, int periodOffset) {
        return clone(get(key, periodOffset))
    }

    public Object getCloned(String key) {
        return clone(get(key, 0))
    }

    private Object clone(Collection collection) {
        Collection clonedCollection = (Collection) collection.clone()
        clonedCollection.clear()
        for (Object obj: collection) {
            //todo(sku): implement a deep clone
            clonedCollection.add(obj.clone())
        }
        return clonedCollection
    }

    private Object clone(Object collection) {
        return collection.clone()
    }

    public boolean exists(String key) {
        return store.get(key) != null
    }

    /**
     * @param key
     * @param fromPeriod
     * @param toPeriod
     * @return all object of key from period to period
     */
    public List get(String key, int fromPeriod, int toPeriod) {
        List result = []
        int fromPeriodOffset = fromPeriod - periodScope.currentPeriod
        int toPeriodOffset = toPeriod - periodScope.currentPeriod
        for (int i = fromPeriodOffset; i <= toPeriodOffset; i++) {
            result << get(key, i)
        }
        return result
    }

    /**
     * @param key
     * @return all objects belonging to key
     */
    public List getAll(String key) {
        return get(key, 0, indexOfLastItem(key))
    }

    /**
     * @param key
     * @param fromPeriodOffset
     * @return all objects belonging to key starting with fromPeriodOffset
     */
    public List getAll(String key, int fromPeriodOffset) {
        return get(key, fromPeriodOffset, indexOfLastItem(key))
    }

    public boolean empty() {
        return store.isEmpty()
    }

    private int indexOfLastItem(String key) {
        return store[key].size() - 1
    }

    protected void clear() {
        store.clear()
    }
}
