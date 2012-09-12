package org.pillarone.riskanalytics.core.simulation;

import org.joda.time.DateTime;

import java.util.Collection;
import java.util.List;

/**
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
public interface ILimitedPeriodCounter extends IPeriodCounter {

    int periodCount();

    List<DateTime> periodDates();
}
