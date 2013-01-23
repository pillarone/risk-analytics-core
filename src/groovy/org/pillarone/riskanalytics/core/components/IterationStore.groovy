package org.pillarone.riskanalytics.core.components;

import org.pillarone.riskanalytics.core.simulation.engine.IterationScope;
import org.pillarone.riskanalytics.core.simulation.engine.PeriodScope;

/**
 *  A IterationStore is used whenever a Component needs a memory in order to access data from former periods/
 *  iterations or prepare data that the Component itself will need in future periods/iterations. Functionality
 *  is close to the PeriodStore, differences are that stored values are not flushed at the end of an iteration
 *  and values can write access is only enabled during the first iteration. Read access is enabled during all
 *  iterations.
 *  In order to enable this memory capability a component needs one member variable:
 *  <code>
 *  IterationStore iterationStore
 *  </code>
 *  It is initialized by injection.
 *
 *  The IterationStore may keep different kind of data, as it accepts objects addressed by keys.<br/>
 *
 *  Usage: Converting complex parameters into business objects in the first only.<br/>
 *  Cave: Modifying objects provided by an IterationStore may lead to side effects.
 *
 *  @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
public class IterationStore extends AbstractStore {

    /** is used to keep period information in sync.      */
    IterationScope iterationScope;

    public IterationStore(IterationScope iterationScope) {
        this.iterationScope = iterationScope;
        initPeriodScope(iterationScope.getPeriodScope());
    }

    @Override
    public void initPeriodScope(PeriodScope periodScope) {
        this.periodScope = periodScope;
    }

    /**
     *  Stores the object into current period + periodOffset. Writing to past period is not permitted. Therefore
     *  periodOffset has to be 0 or positive.
     */
    @Override
    public void put(String key, Object s, int periodOffset) {
        if (!iterationScope.isFirstIteration()) {
            throw new IllegalArgumentException("Write access enabled only during first iteration.");
        }
        super.put(key, s, periodOffset);
    }
}