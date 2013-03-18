package org.pillarone.riskanalytics.core.dataaccess;

import org.joda.time.DateTime;

/**
 * author simon.parten @ art-allianz . com
 */
public class DateTimeValuePair {

    final long dateTime;
    final double aDouble;

    public DateTimeValuePair(long dateTime, double aDouble) {
        this.dateTime = dateTime;
        this.aDouble = aDouble;
    }

    public long getDateTime() {
        return dateTime;
    }

    public double getaDouble() {
        return aDouble;
    }
}
