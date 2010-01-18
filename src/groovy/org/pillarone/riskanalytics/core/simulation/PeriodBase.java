package org.pillarone.riskanalytics.core.simulation;

import org.joda.time.Period;

import java.util.Map;

/**
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
public enum PeriodBase {
    MONTHLY, QUARTERLY, HALF_YEARLY, YEARLY;

    public Object getConstructionString(Map parameters) {
        return getClass().getName() + "." + this;
    }

    /**
     * @return The JodaTime Period equivalent of the enum value
     */
    public Period toPeriod() {
        switch(ordinal()) {
            case 0:
                return Period.months(1);
            case 1:
                return Period.months(3);
            case 2:
                return Period.months(6);
            case 3:
                return Period.years(1);
        }
        return null;
    }
}
