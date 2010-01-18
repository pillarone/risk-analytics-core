package org.pillarone.riskanalytics.core.model

import org.joda.time.Period
import org.pillarone.riskanalytics.core.simulation.IPeriodCounter
import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.simulation.ContinuousPeriodCounter

/**
 * A DeterministicModel is a Model without random elements. Therefore only one iteration is necessary.
 * But the period count can be set by the user.
 */
abstract class DeterministicModel extends Model {

    /**
     * The length of one period of this model.
     */
    abstract Period getPeriodLength()

    /**
     * Creates a period counter with a constant period length
     * @param beginOfFirstPeriod
     *        Begin of the first period defined by the user  
     */
    IPeriodCounter createPeriodCounter(DateTime beginOfFirstPeriod) {
        return new ContinuousPeriodCounter(beginOfFirstPeriod, getPeriodLength())
    }

    boolean requiresStartDate() {
        return true
    }


}