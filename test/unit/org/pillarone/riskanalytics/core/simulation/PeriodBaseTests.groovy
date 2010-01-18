package org.pillarone.riskanalytics.core.simulation

import org.joda.time.Period


class PeriodBaseTests extends GroovyTestCase {

    void testToPeriod() {
        assertEquals Period.months(1), PeriodBase.MONTHLY.toPeriod()
        assertEquals Period.months(3), PeriodBase.QUARTERLY.toPeriod()
        assertEquals Period.months(6), PeriodBase.HALF_YEARLY.toPeriod()
        assertEquals Period.years(1), PeriodBase.YEARLY.toPeriod()
    }
}
