package org.pillarone.riskanalytics.core.example.parameter

import org.pillarone.riskanalytics.core.parameterization.IMultiDimensionalConstraints
import org.pillarone.riskanalytics.core.example.component.ExampleResource


class ExampleResourceConstraints implements IMultiDimensionalConstraints {

    public static final String IDENTIFIER = "resourceConstraints"

    Integer getColumnIndex(Class marker) {
        return null
    }

    boolean matches(int row, int column, Object value) {
        return true
    }

    String getName() {
        return IDENTIFIER
    }

    Class getColumnType(int column) {
        return ExampleResource
    }

    boolean emptyComponentSelectionAllowed(int column) {
        return false
    }
}
