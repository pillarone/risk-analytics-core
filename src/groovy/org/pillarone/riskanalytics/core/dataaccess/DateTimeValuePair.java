package org.pillarone.riskanalytics.core.dataaccess;

import org.joda.time.DateTime;

import java.io.Serializable;

/**
 * author simon.parten @ art-allianz . com
 */
public class DateTimeValuePair implements Serializable {

    final long dateTime;
    final double aDouble;
    final String packetId;

    public DateTimeValuePair(long dateTime, double aDouble, String packetId) {
        this.dateTime = dateTime;
        this.aDouble = aDouble;
        this.packetId = packetId;
    }

    public long getDateTime() {
        return dateTime;
    }

    public double getaDouble() {
        return aDouble;
    }

    public String getPacketId() {
        return packetId;
    }

    @Override
    public String toString() {
        return "DateTimeValuePair{" +
            "dateTime=" + new DateTime(dateTime) +
            ", aDouble=" + aDouble +
            ", packetId='" + packetId + '\'' +
            '}';
    }
}
