package org.pillarone.riskanalytics.core.example.component

import org.pillarone.riskanalytics.core.parameterization.TableMultiDimensionalParameter
import org.pillarone.riskanalytics.core.components.Component

/**
 * This component is used for testing StochasticModel with variable period lengths.
 *
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
class TestListHeaderDateTimeComponent extends Component {

    TableMultiDimensionalParameter parmPayments = new TableMultiDimensionalParameter(
            [["ABC001"], [100d], [10d], [150d], [120d], [180d]],
            ["Policy ID", "2009-11-01", "2010-02-01", "2010-05-01", "2010-08-01", "2010-11-01"]);

    protected void doCalculation() {
    }
}