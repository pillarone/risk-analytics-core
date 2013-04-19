package org.pillarone.riskanalytics.core.parameterization

import groovy.transform.CompileStatic

/**
 * @author fouad.jaada@intuitive-collaboration.com
 */
@CompileStatic
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

    boolean emptyComponentSelectionAllowed(int column) {
        return false
    }
}
