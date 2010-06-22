package org.pillarone.riskanalytics.core.components;

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
 * @author dierk.koenig (at) canoo (dot) com, stefan.kunz (at) intuitive-collaboration (dot) com
 */
public class PeriodStore extends AbstractStore {

    public PeriodStore(PeriodScope periodScope) {
        initPeriodScope(periodScope);
    }

    @Override
    public void initPeriodScope(PeriodScope periodScope) {
        this.periodScope = periodScope;
    }

    public void clear() {
        super.clear();
    }
}