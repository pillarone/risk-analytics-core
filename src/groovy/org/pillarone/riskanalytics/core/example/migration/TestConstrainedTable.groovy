package org.pillarone.riskanalytics.core.example.migration

import org.pillarone.riskanalytics.core.parameterization.IMultiDimensionalConstraints
import org.pillarone.riskanalytics.core.parameterization.ConstrainedMultiDimensionalParameter
import org.pillarone.riskanalytics.core.parameterization.ConstraintsFactory

/**
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
class TestConstrainedTable implements IMultiDimensionalConstraints {

    private static final String ID = 'id'
    /*
    Simulate removed column
     */
//    private static final String TYPE = 'type'
    private static final String VALUE = 'value'

    public static final String IDENTIFIER = "constrained table"
    public static final List COLUMN_HEADERS = [ID,/* TYPE, */VALUE]
    public static final int ID_COLUMN_INDEX = 0;
//    public static final int TYPE_COLUMN_INDEX = 1;
    public static final int VALUE_COLUMN_INDEX = 1 //2;

    static ConstrainedMultiDimensionalParameter getDefault()  {
        return new ConstrainedMultiDimensionalParameter(
            [['S1','S2']/*,['Motor','Engine']*/,[100d, 80d]], COLUMN_HEADERS, ConstraintsFactory.getConstraints(IDENTIFIER)
        )
    }

    boolean matches(int row, int column, Object value) {
        switch (column) {
            case ID_COLUMN_INDEX:
            /*case TYPE_COLUMN_INDEX:
                return value instanceof String*/
            case VALUE_COLUMN_INDEX:
                return value instanceof Double
        }
    }

    String getName() {
        IDENTIFIER
    }

    Class getColumnType(int column) {
        [String, /*String, */Double].get(column)
    }

    Integer getColumnIndex(Class marker) {
        null
    }

    boolean emptyComponentSelectionAllowed(int column) {
        return false
    }
}
