package org.pillarone.riskanalytics.core.parameterization;

import org.joda.time.DateTime;
import org.pillarone.riskanalytics.core.components.ComponentUtils;
import org.pillarone.riskanalytics.core.model.Model;
import org.pillarone.riskanalytics.core.util.CloneSupport;
import org.pillarone.riskanalytics.core.util.GroovyUtils;

import java.io.Serializable;
import java.util.*;

public abstract class AbstractMultiDimensionalParameter implements Cloneable, Serializable {

    /**
     * Outer list contains the columns. In order to access the cell in column 5 and row 2, use values.get(5).get(2)
     */
    protected List<List> values;
    protected transient Model simulationModel;
    public int max_tokens = 500;

    /**
     * @param cellValues can be a normal or nested list and is converted to a nested list if provided as normal list
     */
    public AbstractMultiDimensionalParameter(List cellValues) {
        Iterator iterator = cellValues.iterator();
        boolean listFound = false;
        while (iterator.hasNext() && !listFound) {
            if (iterator.next() instanceof List) {
                listFound = true;
            }
        }
        if (listFound) {
            this.values = new ArrayList<List>(cellValues);
        } else {
            ArrayList<List> list = new ArrayList<List>(1);
            list.add(new ArrayList(cellValues));
            this.values = list;
        }
    }

    public boolean isEmpty() {
        if (values.size() == 0) {
            return true;
        }
        for (List list : values) {
            if (!list.isEmpty()) {
                return false;
            }
        }

        return true;
    }


    public int getColumnCount() {
        return getValueColumnCount() + getTitleColumnCount();
    }

    public int getValueColumnCount() {
        return values.size();
    }

    public abstract int getTitleColumnCount();

    public int getRowCount() {
        return getValueRowCount() + getTitleRowCount();
    }

    public int getValueRowCount() {
        int maxLength = 0;
        for (List list : values) {
            maxLength = Math.max(maxLength, list.size());
        }
        return maxLength;
    }

    public abstract int getTitleRowCount();

    public Object getValueAt(int row, int column) {
        Object object;
        try {
            object = values.get(column).get(row);
        } catch (IndexOutOfBoundsException e) {
            object = "";
        }
        return object;
    }

    public void setValueAt(Object value, int row, int column) {
        if (row >= getValueRowCount() || column >= getValueColumnCount()) {
            throw new IndexOutOfBoundsException();
        }
        List col = values.get(column);
        if (row >= col.size()) {
            col.add(value);
        } else {
            col.set(row, value);
        }
    }

    public List getValues() {
        return values;
    }

    public void addColumnAt(int columnIndex) {
    }

    public void removeColumnAt(int columnIndex) {
    }

    public void addRowAt(int columnIndex) {
    }

    public void removeRowAt(int columnIndex) {
    }

    public void moveColumnTo(int from, int to) {
        Collections.swap(values, from, to);
    }

    public void moveRowTo(int from, int to) {
        for (List rowList : values)
            Collections.swap(rowList, from, to);
    }


    protected void setDiagonalValue() {
    }

    protected abstract void rowsAdded(int i);

    protected abstract void columnsAdded(int i);

    protected abstract void rowsRemoved(int i);

    protected abstract void columnsRemoved(int i);

    public void setDimension(MultiDimensionalParameterDimension dimension) {
        int newRowCount = dimension.getRows();
        int newColumnCount = dimension.getColumns();

        int currentRowCount = getValueRowCount();
        int currentColumnCount = getValueColumnCount();

        if (newColumnCount > currentColumnCount) {
            for (int i = 0; i < (newColumnCount - currentColumnCount); i++) {
                addColumn(currentColumnCount);
            }
            columnsAdded(newColumnCount - currentColumnCount);
        }

        if (newRowCount > currentRowCount) {

            int columnCount = getValueColumnCount();
            for (int currentColumn = 0; currentColumn < columnCount; currentColumn++) {
                if (currentColumn >= values.size()) {
                    addColumn(currentColumn);
                }
                List list = values.get(currentColumn);
                if (list.size() == currentRowCount) {
                    for (int i = 0; i < (newRowCount - currentRowCount); i++) {
                        list.add(createDefaultValue(currentRowCount + i, currentColumn, null));
                    }
                }
            }
            rowsAdded(newRowCount - currentRowCount);
        }

        if (newRowCount < currentRowCount) {

            int shrinkCount = currentRowCount - newRowCount;
            for (List list : values) {
                if (list.size() == currentRowCount) {
                    for (int i = 0; i < shrinkCount; i++) {
                        list.remove(list.size() - 1);
                    }
                }
            }
            rowsRemoved(currentRowCount - newRowCount);
        }

        if (newColumnCount < currentColumnCount) {
            for (int i = 0; i < (currentColumnCount - newColumnCount); i++) {
                values.remove(values.size() - 1);
            }
            columnsRemoved(currentColumnCount - newColumnCount);
        }
        setDiagonalValue();
    }

    private void addColumn(int currentColumnCount) {
        List lastList = values.get(values.size() - 1);
        ArrayList newList = new ArrayList();
        int rowIndex = 0;
        for (Object object : lastList) {
            newList.add(createDefaultValue(rowIndex, currentColumnCount, object));
            rowIndex++;
        }
        values.add(newList);
    }

    protected Object createDefaultValue(int row, int column, Object object) {

        object = (object == null && values.get(column).size() > 0) ? values.get(column).get(0) : new Double(0);
        if (object instanceof Date) return ((Date) object).clone();
        if (object instanceof DateTime) return new DateTime(((DateTime) object).getMillis());
        if (object instanceof String) return object;
        if (object instanceof Integer) return new Integer(0);
        return new Double(0);
    }

    public boolean supportsZeroRows() {
        return false;
    }

    public abstract boolean isCellEditable(int row, int column);

    public String toString() {
        StringBuffer buffer = new StringBuffer("new ");
        buffer.append(this.getClass().getName());
        buffer.append("(");
        buffer.append("org.pillarone.riskanalytics.core.util.GroovyUtils.toList(" + GroovyUtils.listToString(GroovyUtils.getSplitList(values, max_tokens)) + ")");
        appendAdditionalConstructorArguments(buffer);
        buffer.append(")");
        return buffer.toString();

    }


    protected abstract void appendAdditionalConstructorArguments(StringBuffer buffer);

    protected void appendList(StringBuffer result, List listValues) {
        if (listValues == null) {
            result.append("null");
        } else {
            result.append("[");
            for (Object o : listValues) {
                result.append(getValue(o));
                result.append(",");
            }
            if (!listValues.isEmpty()) {
                result.deleteCharAt(result.length() - 1);
            }
            result.append("]");
        }
    }

    private String getValue(Object value) {
        if (value instanceof String) {
            StringBuffer buffer = new StringBuffer("\"");
            buffer.append(value);
            buffer.append("\"");
            return buffer.toString();
        } else {
            return value.toString();
        }
    }

    public void setSimulationModel(Model simulationModel) {
        this.simulationModel = simulationModel;
    }

    public Model getSimulationModel() {
        return simulationModel;
    }

    public Object getPossibleValues(int row, int column) {
        return getValueAt(row, column);
    }

    public List getColumnNames() {
        return new ArrayList();
    }

    public List getRowNames() {
        return new ArrayList();
    }

    protected String normalizeName(String name) {
        return ComponentUtils.getNormalizedName(name);
    }

    public abstract boolean columnCountChangeable();

    public abstract boolean rowCountChangeable();

    @Override
    public AbstractMultiDimensionalParameter clone() throws CloneNotSupportedException {
        final AbstractMultiDimensionalParameter clone = (AbstractMultiDimensionalParameter) super.clone();
        clone.simulationModel = null;
        clone.values = new ArrayList<List>(values.size());
        for (List list : values) {
            clone.values.add(CloneSupport.deepClone(list));
        }
        return clone;
    }
}


