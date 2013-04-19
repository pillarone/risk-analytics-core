package org.pillarone.riskanalytics.core.components

import groovy.transform.CompileStatic
import org.pillarone.riskanalytics.core.example.marker.ITest2ComponentMarker
import org.pillarone.riskanalytics.core.example.marker.ITestComponentMarker
import org.pillarone.riskanalytics.core.parameterization.IMultiDimensionalConstraints

@CompileStatic
class ExampleMultiMarkerConstraint implements IMultiDimensionalConstraints {

    public static final String NAME = "ExampleMultiMarkerConstraint"

    private final List<Class<? extends IComponentMarker>> columns = [ITestComponentMarker, ITest2ComponentMarker]

    @Override
    boolean matches(int row, int column, Object value) {
        return true
    }

    @Override
    String getName() {
        return NAME
    }

    @Override
    Class getColumnType(int column) {
        return columns[column]
    }

    @Override
    Integer getColumnIndex(Class marker) {
        return columns.indexOf(marker)
    }

    boolean emptyComponentSelectionAllowed(int column) {
        return true
    }
}
