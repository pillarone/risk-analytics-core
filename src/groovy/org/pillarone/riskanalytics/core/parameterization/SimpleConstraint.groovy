package org.pillarone.riskanalytics.core.parameterization

/**
 * @author fouad.jaada@intuitive-collaboration.com
 */
class SimpleConstraint implements IMultiDimensionalConstraints {

    public static final String IDENTIFIER = "SIMPLE_CONSTRAINT"

    boolean matches(int row, int column, Object value) {
        return value instanceof Number
    }

    String getName() {
        return IDENTIFIER
    }

    Class getColumnType(int column) {
        return BigDecimal
    }


    Integer getColumnIndex(Class marker) {
        return null;
    }
}
