package org.pillarone.riskanalytics.core.model

import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolder
import org.pillarone.riskanalytics.core.simulation.IPeriodCounter
import org.joda.time.DateTime

/**
 * A model with random elements. The iteration count is specified by the user.
 */
abstract class StochasticModel extends Model {

    /**
     * If the simulation period count is different than the parameterization period count,
     * the model should override this method.
     */
    int getSimulationPeriodCount(List<ParameterHolder> parameters, int parameterPeriodCount) {
        return parameterPeriodCount
    }

    /**
     * Stochastic models usually do not have a period counter. If they need one the model should override
     * this method.
     */
    IPeriodCounter createPeriodCounter(DateTime beginOfFirstPeriod) {
        return null;
    }

    boolean requiresStartDate() {
        return false
    }


}
