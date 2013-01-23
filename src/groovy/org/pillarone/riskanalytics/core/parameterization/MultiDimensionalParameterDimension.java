package org.pillarone.riskanalytics.core.parameterization;

public class MultiDimensionalParameterDimension {
    int rows;
    int columns;

    public MultiDimensionalParameterDimension(int columns, int rows) {
        this.columns = columns;
        this.rows = rows;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getColumns() {
        return columns;
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }
}
