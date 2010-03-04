package org.pillarone.riskanalytics.core.simulation

import org.joda.time.DateTime
import org.joda.time.Period

/**
 * A period counter with a fixed number of periods all of the same length.
 *
 * @author stefan.kunz (at) intuitive-collaboration (dot) com 
 */
class LimitedContinuousPeriodCounter extends ContinuousPeriodCounter implements ILimitedPeriodCounter {

    private List<DateTime> dates = []
    private int numberOfPeriods

    public LimitedContinuousPeriodCounter(DateTime simulationStartDate, Period periodLength, int numberOfPeriods) {
        super(simulationStartDate, periodLength);
        this.numberOfPeriods = numberOfPeriods
    }

    int periodCount() {
        numberOfPeriods
    }
}