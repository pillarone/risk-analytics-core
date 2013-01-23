package org.pillarone.riskanalytics.core.parameterization


interface IMarkerBasedMultiDimensionalParameter {

    boolean isMarkerCell(int row, int column)

    void validateValues()

}
