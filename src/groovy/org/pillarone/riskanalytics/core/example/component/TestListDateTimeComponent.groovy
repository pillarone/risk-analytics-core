package org.pillarone.riskanalytics.core.example.component

import org.pillarone.riskanalytics.core.parameterization.TableMultiDimensionalParameter
import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.simulation.engine.PeriodScope

/**
 * This component is used for testing StochasticModel with variable period lengths.
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */

class TestListDateTimeComponent extends Component {

    PeriodScope periodScope

    TableMultiDimensionalParameter parmDates = new TableMultiDimensionalParameter(
            ["2009-11-01", "2010-02-01", "2010-05-01", "2010-08-01", "2010-11-01"], ["Payment Dates"]);

    protected void doCalculation() {
    }
}