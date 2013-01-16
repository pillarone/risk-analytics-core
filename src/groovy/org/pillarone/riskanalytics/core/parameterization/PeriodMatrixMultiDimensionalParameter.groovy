package org.pillarone.riskanalytics.core.parameterization

import org.pillarone.riskanalytics.core.components.Component


class PeriodMatrixMultiDimensionalParameter extends AbstractMultiDimensionalParameter implements IComboBoxBasedMultiDimensionalParameter {

    Class markerClass
    List titles

    PeriodMatrixMultiDimensionalParameter(List cellValues, List rowTitles, Class markerClass) {
        super(cellValues)
        titles = rowTitles
        this.markerClass = markerClass
    }

    @Override
    void validateValues() {

    }

    @Override
    int getTitleColumnCount() {
        return 2
    }

    @Override
    int getTitleRowCount() {
        return 2
    }

    @Override
    boolean isCellEditable(int row, int column) {
        return !(row < getTitleRowCount() && column < getTitleColumnCount())
    }

    public Object getValueAt(int row, int column) {
        if (row < getTitleRowCount() && column < getTitleColumnCount()) {
            return "";
        }

        if (row < getTitleRowCount()) {
            List titleRow = titles.get(row)
            return titleRow.get(column - getTitleColumnCount())
        }
        if (column < getTitleColumnCount()) {
            List titleColumn = titles.get(column)
            return titleColumn.get(row - getTitleRowCount())
        }
        return super.getValueAt(row - getTitleRowCount(), column - getTitleColumnCount());
    }

    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        if (rowIndex >= getTitleRowCount() && columnIndex >= getTitleColumnCount()) {
            super.setValueAt(value, rowIndex - getTitleRowCount(), columnIndex - getTitleColumnCount());
            super.setValueAt(value, columnIndex - getTitleColumnCount(), rowIndex - getTitleRowCount());
        } else if (!(rowIndex < getTitleRowCount() && columnIndex < getTitleColumnCount())) {
            if (columnIndex < getTitleColumnCount()) {
                List cList = titles.get(columnIndex)
                cList.set(rowIndex - getTitleRowCount(), value)
            } else {
                List cList = titles.get(rowIndex)
                cList.set(columnIndex - getTitleColumnCount(), value)
            }
        }
    }

    public Object getPossibleValues(int row, int column) {
        if (row < getTitleRowCount() && column < getTitleColumnCount()) {
            return "";
        }

        if (row == 0 || column == 0) {
            List<String> names = new ArrayList<String>();
            List<Component> markedComponents = simulationModel.getMarkedComponents(markerClass);
            for (Component c : markedComponents) {
                names.add(c.getName());
            }
            return names;
        } else if (row == 1 || column == 1) {
            return 1..10
        } else {
            return getValueAt(row, column);
        }
    }

    @Override
    List getRowNames() {
        return titles
    }

    @Override
    protected void appendAdditionalConstructorArguments(StringBuffer buffer) {
        buffer.append(",").append(titles.toListString()).append(",").append(markerClass.name)
    }

    @Override
    protected void rowsAdded(int i) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void columnsAdded(int i) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void rowsRemoved(int i) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void columnsRemoved(int i) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    boolean columnCountChangeable() {
        return false  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    boolean rowCountChangeable() {
        return false  //To change body of implemented methods use File | Settings | File Templates.
    }
}
