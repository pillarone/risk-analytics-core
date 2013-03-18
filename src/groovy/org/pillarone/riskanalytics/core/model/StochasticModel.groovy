package org.pillarone.riskanalytics.core.model

import org.pillarone.riskanalytics.core.simulation.IPeriodCounter
import org.joda.time.DateTime

/**
 * A model with random elements. The iteration count is specified by the user.
 */
abstract class StochasticModel extends Model {

    /**
     * Stochastic models usually do not have a period counter. If they need one the model should override
     * this method. If this method returns a fixed length period counter, it is also used to determine the
     * number of simulation periods.
     */
    IPeriodCounter createPeriodCounter(DateTime beginOfFirstPeriod) {
        return null;
    }

    boolean requiresStartDate() {
        return false
    }


}
