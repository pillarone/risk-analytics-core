package org.pillarone.riskanalytics.core.simulation

import groovy.transform.CompileStatic
import org.joda.time.DateTime
import org.joda.time.Period

/**
 * A period counter with a fixed number of periods all of the same length.
 *
 * @author stefan.kunz (at) intuitive-collaboration (dot) com 
 */
@CompileStatic
class LimitedContinuousPeriodCounter extends ContinuousPeriodCounter implements ILimitedPeriodCounter {

    private List<DateTime> dates = new ArrayList<DateTime>()
    private int numberOfPeriods

    public LimitedContinuousPeriodCounter(DateTime simulationStartDate, Period periodLength, int numberOfPeriods) {
        super(simulationStartDate, periodLength);
        this.numberOfPeriods = numberOfPeriods
    }

    int periodCount() {
        numberOfPeriods
    }

    boolean dateInSimulationScope(DateTime date) {
        if (date.isBefore(startOfFirstPeriod())) return false
        if (date.isAfter(endOfLastPeriod())) return false
        return true
    }

    DateTime endOfLastPeriod() {
        endOfPeriod(numberOfPeriods)
    }

    @Override
    String toString() {
        return startOfFirstPeriod().toString() + ", number of periods: " + periodCount()
    }

    List<DateTime> periodDates() {
        if (dates.size() > 0) {
            return dates
        } else {
            DateTime aDate = getPeriodStart(0)
            for (int i = 0; i <= numberOfPeriods + 1; i++) {
                dates << aDate
                aDate = aDate.plus(getPeriodLength())
            }
        }
        return dates
    }
}