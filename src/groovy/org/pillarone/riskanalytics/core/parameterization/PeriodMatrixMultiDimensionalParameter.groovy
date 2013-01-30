package org.pillarone.riskanalytics.core.parameterization

import org.apache.commons.lang.builder.EqualsBuilder
import org.apache.commons.lang.builder.HashCodeBuilder
import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.model.Model


class PeriodMatrixMultiDimensionalParameter extends AbstractMultiDimensionalParameter implements IComboBoxBasedMultiDimensionalParameter {

    private Map<String, Component> componentMap = new HashMap<String, Component>();

    Class markerClass
    List titles

    PeriodMatrixMultiDimensionalParameter(List cellValues, List rowTitles, Class markerClass) {
        super(cellValues)
        titles = rowTitles
        this.markerClass = markerClass
    }

    public void setSimulationModel(Model simulationModel) {
        super.setSimulationModel(simulationModel);
        if (simulationModel != null) {
            List<Component> components = simulationModel.getMarkedComponents(markerClass);
            componentMap.clear();
            for (Component component : components) {
                componentMap.put(component.getName(), component);
            }
        } else {
            componentMap.clear();
        }
    }

    @Override
    void validateValues() {
        List<String> validValues = []

        List componentList = titles[0]
        for (String value in componentList) {
            if (componentMap.keySet().contains(value)) {
                validValues << value
            }
        }

        if (validValues.size() < componentList.size()) {
            updateTable(getMaxPeriod(), validValues)
        }
    }

    public boolean isMarkerCell(int row, int column) {
        return (row == 0 && column > 0) || (column == 0 && row > 0);
    }

    int getMaxPeriod() {
        return titles[1].isEmpty() ? 0 : titles[1].collect { Integer.parseInt(it.toString()) }.max()
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
            return titleRow.isEmpty() ? "" : titleRow.get(column - getTitleColumnCount())
        }
        if (column < getTitleColumnCount()) {
            List titleColumn = titles.get(column)
            return titleColumn.isEmpty() ? "" : titleColumn.get(row - getTitleRowCount())
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
    }

    @Override
    protected void columnsAdded(int i) {
    }

    @Override
    protected void rowsRemoved(int i) {
    }

    @Override
    protected void columnsRemoved(int i) {
    }

    @Override
    boolean columnCountChangeable() {
        return false
    }

    @Override
    boolean rowCountChangeable() {
        return false
    }

    void updateTable(int periodCount, List<String> components) {
        titles = [[], []]
        values = []
        int columns = periodCount * components.size()
        for (int i = 0; i < columns; i++) {
            List column = []
            columns.times { column << 0d }
            values << column
            values[i][i] = 1d
        }

        for (String component in components) {
            for (int i = 0; i < periodCount; i++) {
                titles[0] << component
                titles[1] << String.valueOf(i + 1)
            }
        }
        validateValues()
    }

    List<CorrelationInfo> getCorrelations() {
        Set<CorrelationInfo> result = new HashSet<CorrelationInfo>()

        int i = 0
        for (List column in values) {
            int j = 0
            for (double cell in column) {
                if (i != j) {
                    result << new CorrelationInfo(
                            component1: componentMap.get(titles[0][i]),
                            component2: componentMap.get(titles[0][j]),
                            period1: Integer.parseInt(titles[1][i]),
                            period2: Integer.parseInt(titles[1][j]),
                            value: values[i][j]
                    )
                }
                j++
            }
            i++
        }

        return result.toList()
    }

    public static class CorrelationInfo {
        Component component1
        int period1
        Component component2
        int period2
        double value

        @Override
        int hashCode() {
            return new HashCodeBuilder().
                    append(component1.name).append(component2.name).
                    append(period1).append(period2).toHashCode()
        }

        @Override
        boolean equals(Object obj) {
            if (obj instanceof CorrelationInfo) {
                return new EqualsBuilder().append(component1, obj.component1).append(component2, obj.component2).
                        append(period1, obj.period1).append(period2, obj.period2).
                        equals
            }

            return false
        }

        @Override
        String toString() {
            return "${component1.name} (P${period1}) ${component2.name} (P${period2}) ${value}"
        }
    }
}
