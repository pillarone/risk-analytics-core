package org.pillarone.riskanalytics.core.parameterization;

import java.util.List;

public class MatrixMultiDimensionalParameter extends AbstractMultiDimensionalParameter {

    protected List columnTitles;
    protected List rowTitles;

    public MatrixMultiDimensionalParameter(List cellValues, List rowTitles, List columnTitles) {
        super(cellValues);
        this.columnTitles = columnTitles;
        this.rowTitles = rowTitles;
    }


    public int getTitleColumnCount() {
        return 1;
    }

    public int getTitleRowCount() {
        return 1;
    }

    public boolean isCellEditable(int row, int column) {
        return row > 0 && column > 0;
    }

    public Object getValueAt(int row, int column) {
        if (row == 0 && column == 0) {
            return "";
        }

        if (row == 0) {
            return columnTitles.get(column - getTitleRowCount()).toString();
        }
        if (column == 0) {
            return rowTitles.get(row - getTitleColumnCount()).toString();
        }

        return super.getValueAt(row - 1, column - 1);
    }

    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        if (rowIndex > 0 && columnIndex > 0) {
            super.setValueAt(value, rowIndex - 1, columnIndex - 1);
        }
    }

    protected void rowsAdded(int i) {
        for (int j = 0; j < i; j++) {
            rowTitles.add("New title");
        }
    }

    protected void columnsAdded(int i) {
        for (int j = 0; j < i; j++) {
            columnTitles.add("New title");
        }
    }

    protected void rowsRemoved(int i) {
        for (int j = 0; j < i; j++) {
            if (rowTitles.size() > getValueRowCount()) {
                rowTitles.remove(rowTitles.size() - 1);
            }
        }
    }

    protected void columnsRemoved(int i) {
        for (int j = 0; j < i; j++) {
            if (columnTitles.size() > getValueColumnCount()) {
                columnTitles.remove(columnTitles.size() - 1);
            }
        }
    }

    protected void appendAdditionalConstructorArguments(StringBuffer buffer) {
        buffer.append(",");
        appendList(buffer, rowTitles);

        buffer.append(",");
        appendList(buffer, columnTitles);
    }

    public List getColumnByName(String name) {
        int index = columnTitles.indexOf(name);
        if (index < 0) {
            throw new IllegalArgumentException("Column '$name' not found");
        }
        return values.get(index);
    }

    public List getRowNames() {
        return rowTitles;
    }

    public List getColumnNames() {
        return columnTitles;
    }

    public boolean columnCountChangeable() {
        return false;
    }

    public boolean rowCountChangeable() {
        return false;
    }
}
