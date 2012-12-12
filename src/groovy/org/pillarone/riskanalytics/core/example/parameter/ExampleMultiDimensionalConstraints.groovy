package org.pillarone.riskanalytics.core.example.parameter

import org.pillarone.riskanalytics.core.parameterization.IMultiDimensionalConstraints
import org.pillarone.riskanalytics.core.example.marker.ITestComponentMarker

/**
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
class ExampleMultiDimensionalConstraints implements IMultiDimensionalConstraints {

    public static final String EXAMPLE_MDC = "Example Multi Dimensional Constraints"

    boolean matches(int row, int column, Object value) {
        if (column == 0) {
            return value instanceof String
        }
        else if (column == 1) {
            return value instanceof Number
        }
        return false
    }

    String getName() {
        return EXAMPLE_MDC
    }

    Class getColumnType(int column) {
        return column == 0 ? ITestComponentMarker : BigDecimal
    }

    Integer getColumnIndex(Class marker) {
        if (ITestComponentMarker.isAssignableFrom(marker)) {
            return 0
        }
        else if (BigDecimal.isAssignableFrom(marker)) {
            return 1
        }
        return null;
    }

    boolean emptyComponentSelectionAllowed(int column) {
        return false
    }
}
