package org.pillarone.riskanalytics.core.parameterization

interface IMultiDimensionalConstraints {

    boolean matches(int row, int column, Object value)

    String getName()

    Class getColumnType(int column)

}