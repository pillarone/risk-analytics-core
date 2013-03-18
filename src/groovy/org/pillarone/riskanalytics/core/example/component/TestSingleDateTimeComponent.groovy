package org.pillarone.riskanalytics.core.example.component

import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.components.Component

/**
 * This component is used for testing StochasticModel with variable period lengths.
 *
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
class TestSingleDateTimeComponent extends Component {

    DateTime parmReportDate = new DateTime(2010,1,8,0,0,0,0)

    protected void doCalculation() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
