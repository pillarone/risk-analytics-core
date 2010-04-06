package org.pillarone.riskanalytics.core.parameterization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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
        if (row == 0 && (column == 0 || column == 1)) {
            return "";
        }

        if (row == 0) {
            //-1 for index column
            return columnTitles.get(column - getTitleRowCount() - 1).toString();
        }
        if (column == 0)
            return row;
        if (column == 1) {
            return rowTitles.get(row - getTitleColumnCount()).toString();
        }
        //-2 for index and combobox columns
        return super.getValueAt(row - 1, column - 2);
    }

    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        if (rowIndex > 0 && columnIndex > 0) {
            super.setValueAt(value, rowIndex - 1, columnIndex - 1);
        }
    }

    public void addColumnAt(int columnIndex) {
        List emptyList = new ArrayList();
        for (int i = 1; i < getRowCount(); i++) {
            emptyList.add(generateValue(values.get(0), i - 1));
        }

        if (columnIndex >= values.size()) {
            values.add(emptyList);
            //rowTitle and colTitles are the same object
            if (columnTitles.size() < values.size())
                columnTitles.add(columnTitles.get(0));
        } else {
            values.add(columnIndex, emptyList);
            if (columnTitles.size() < values.size())
                columnTitles.add(columnIndex, columnTitles.get(0));
        }
        setDiagonalValue();
    }

    public void removeColumnAt(int columnIndex) {
        values.remove(columnIndex);
//        columnTitles.remove(columnIndex);
    }

    public void addRowAt(int rowIndex) {
        for (List list : values) {
            if (rowIndex >= list.size())
                list.add(generateValue(list, list.size() - 1));
            else
                list.add(rowIndex, generateValue(list, rowIndex));
        }
        if (rowIndex >= values.get(0).size()) {
            if (rowTitles.size() < values.size())
                rowTitles.add(rowTitles.get(0));
        } else {
            if (rowTitles.size() < values.size())
                rowTitles.add(rowIndex, rowTitles.get(0));
        }
        setDiagonalValue();
    }

    public void removeRowAt(int rowIndex) {
        for (List list : values) {
            list.remove(rowIndex);
        }
        rowTitles.remove(rowIndex);
    }

    public void moveColumnTo(int from, int to) {
        super.moveColumnTo(from, to);
        Collections.swap(columnTitles, from, to);
    }

    public void moveRowTo(int from, int to) {
        super.moveRowTo(from, to);
        Collections.swap(rowTitles, from, to);
    }

    public void setDiagonalValue() {
        if (getRowCount() != getColumnCount()) return;
        int column = 0;
        for (List list : values) {
            if (list.size() > column)
                values.get(column).set(column, new Double(1));
            column++;
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

    public static Object generateValue(List list, int index) {
        Object o = list.get(index);
        if (o instanceof Date) return ((Date) o).clone();
        if (o instanceof String) return o;
        if (o instanceof Integer) return new Integer(0);
        return new Double(0);
    }

    @Override
    public MatrixMultiDimensionalParameter clone() throws CloneNotSupportedException {
        final MatrixMultiDimensionalParameter clone = (MatrixMultiDimensionalParameter) super.clone();
        clone.columnTitles = new ArrayList(columnTitles.size());
        for (Object o : columnTitles) {
            clone.columnTitles.add(o);
        }
        clone.rowTitles = new ArrayList(rowTitles.size());
        for (Object o : rowTitles) {
            clone.rowTitles.add(o);
        }
        return clone;
    }
}
