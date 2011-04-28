package org.pillarone.riskanalytics.core.example.migration

import org.pillarone.riskanalytics.core.parameterization.IParameterObjectClassifier
import org.pillarone.riskanalytics.core.parameterization.ConstrainedMultiDimensionalParameter
import org.pillarone.riskanalytics.core.parameterization.AbstractParameterObject

/**
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
class TestConstrainedTableStrategy extends AbstractParameterObject{

    ConstrainedMultiDimensionalParameter table = TestConstrainedTable.getDefault()
    /*
    Simulated migration: mode removed
     */
//    ResultViewMode mode = ResultViewMode.INCREMENTAL

    IParameterObjectClassifier getType() {
        TestConstraintsTableType.TWO_COLUMNS
    }

    Map getParameters() {
        [table: table,
                /*mode : mode*/]
    }

}
