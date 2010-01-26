package org.pillarone.riskanalytics.core.parameterization;

import org.pillarone.riskanalytics.core.model.Model;
import org.pillarone.riskanalytics.core.util.GroovyUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public abstract class AbstractMultiDimensionalParameter {

    protected List<List> values;
    protected Model simulationModel;
    protected boolean valuesConverted = false;
    public int max_tokens = 500;

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
            valuesConverted = true;
            this.values = list;
        }
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
        return valuesConverted ? values.get(0) : values;
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
                List lastList = values.get(values.size() - 1);
                ArrayList newList = new ArrayList();
                for (Object object : lastList) {
                    if (object instanceof Date) {
                        object = ((Date) object).clone();
                    }
                    newList.add(object);
                }
                values.add(newList);
            }
            valuesConverted = false;
            columnsAdded(newColumnCount - currentColumnCount);
        }

        if (newRowCount > currentRowCount) {

            Iterator<List> iterator = values.iterator();
            int currentColumn = 0;
            while (iterator.hasNext()) {
                List list = iterator.next();
                if (list.size() == currentRowCount) {
                    for (int i = 0; i < (newRowCount - currentRowCount); i++) {
                        list.add(createDefaultValue(currentColumn));
                    }
                }
                currentColumn++;
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
    }

    protected Object createDefaultValue(int column) {
        return values.get(column).get(0);
    }

    public boolean supportsZeroRows() {
        return false;
    }

    public abstract boolean isCellEditable(int row, int column);

    public String toString() {
        StringBuffer buffer = new StringBuffer("new ");
        buffer.append(this.getClass().getName());
        buffer.append("(");
        if (!valuesConverted) {
            buffer.append("org.pillarone.riskanalytics.core.util.GroovyUtils.toList(" + GroovyUtils.listToString(GroovyUtils.getSplitList(values, max_tokens)) + ")");
        } else {
            buffer.append("org.pillarone.riskanalytics.core.util.GroovyUtils.toList(" + GroovyUtils.listToString(GroovyUtils.getSplitList(values.get(0), max_tokens)) + ")");
        }
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

    public boolean isValuesConverted() {
        return valuesConverted;
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
        String value = name;

        if (value == null) {
            return null;
        }

        if (value.startsWith("sub")) {
            value = value.substring(3);
        }
        if (value.startsWith("parm")) {
            value = value.substring(4);
        }
        if (value.startsWith("out")) {
            value = value.substring(3);
        }

        StringBuffer displayNameBuffer = new StringBuffer();

        int index = 0;
        for (char c : value.toCharArray()) {
            if (Character.isUpperCase(c) && index == 0) {
                displayNameBuffer.append(Character.toLowerCase(c));
            } else if (Character.isUpperCase(c)) {
                displayNameBuffer.append(" ").append(Character.toLowerCase(c));
            } else {
                displayNameBuffer.append(c);
            }
            index++;
        }
        return displayNameBuffer.toString();
    }

    public abstract boolean columnCountChangeable();

    public abstract boolean rowCountChangeable();
}


