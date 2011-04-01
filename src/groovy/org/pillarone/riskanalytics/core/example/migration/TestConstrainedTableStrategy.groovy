package org.pillarone.riskanalytics.core.example.migration

import org.pillarone.riskanalytics.core.parameterization.IParameterObject
import org.pillarone.riskanalytics.core.parameterization.IParameterObjectClassifier
import org.pillarone.riskanalytics.core.parameterization.ConstrainedMultiDimensionalParameter

/**
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
class TestConstrainedTableStrategy implements IParameterObject {

    ConstrainedMultiDimensionalParameter table = TestConstrainedTable.getDefault()
    /*
    Simulated migration: mode removed
     */
//    ResultViewMode mode = ResultViewMode.INCREMENTAL

    IParameterObjectClassifier getType() {
        TestConstraintsTableType.THREE_COLUMNS
    }

    Map getParameters() {
        [table: table,
                /*mode : mode*/]
    }

}
