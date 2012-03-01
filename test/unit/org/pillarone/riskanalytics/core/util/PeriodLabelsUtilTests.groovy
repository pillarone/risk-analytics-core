package org.pillarone.riskanalytics.core.util

import models.core.ResourceModel
import org.joda.time.DateTime
import org.joda.time.Period
import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.simulation.IPeriodCounter
import org.pillarone.riskanalytics.core.simulation.LimitedContinuousPeriodCounter

/**
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
class PeriodLabelsUtilTests extends GroovyTestCase {

    void testGetPeriodLabels() {
        List<String> periodLabels = PeriodLabelsUtil.getPeriodLabels(['2001-01-01', '2002-01-01', '2003-01-01'],
                new DateTime(2001, 1, 1,0,0,0,0), 3, null)
        assertEquals "annual periods, beginOfFirstPeriod", ['2001', '2002', '2003'], periodLabels

        periodLabels = PeriodLabelsUtil.getPeriodLabels(['2001-01-01', '2002-01-01', '2003-01-01'],
                null, 3, new LimitedContinuousPeriodCounterModel())
        assertEquals "annual periods, ContinuousPeriodCounter", ['2001', '2002', '2003'], periodLabels

        periodLabels = PeriodLabelsUtil.getPeriodLabels(null, null, 3, new ResourceModel())
        assertEquals "number of periods only", ['P0', 'P1', 'P2'], periodLabels
    }

    private class LimitedContinuousPeriodCounterModel extends Model {

        @Override
        void initComponents() {}

        @Override
        void wireComponents() {}

        @Override
        IPeriodCounter createPeriodCounter(DateTime beginOfFirstPeriod) {
            return new LimitedContinuousPeriodCounter(beginOfFirstPeriod, Period.years(1), 3)
        }

        @Override
        boolean requiresStartDate() { return false }
    }
}
