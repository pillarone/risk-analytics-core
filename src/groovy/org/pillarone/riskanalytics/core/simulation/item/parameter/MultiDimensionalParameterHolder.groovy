package org.pillarone.riskanalytics.core.simulation.item.parameter

import org.pillarone.riskanalytics.core.parameter.Parameter
import org.pillarone.riskanalytics.core.parameterization.AbstractMultiDimensionalParameter
import org.pillarone.riskanalytics.core.parameter.MultiDimensionalParameter
import org.pillarone.riskanalytics.core.parameterization.ConstrainedMultiDimensionalParameter
import org.pillarone.riskanalytics.core.parameterization.ComboBoxTableMultiDimensionalParameter
import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.parameterization.IMultiDimensionalConstraints

class MultiDimensionalParameterHolder extends ParameterHolder implements IMarkerValueAccessor {

    private AbstractMultiDimensionalParameter value;

    public MultiDimensionalParameterHolder(Parameter parameter) {
        super(parameter.path, parameter.periodIndex);
        this.value = parameter.parameterInstance
    }

    public MultiDimensionalParameterHolder(String path, int periodIndex, AbstractMultiDimensionalParameter value) {
        super(path, periodIndex);
        this.value = value;
    }

    AbstractMultiDimensionalParameter getBusinessObject() {
        return value;
    }

    void applyToDomainObject(Parameter parameter) {
        parameter.parameterInstance = value
    }

    Parameter createEmptyParameter() {
        return new MultiDimensionalParameter(path: path, periodIndex: periodIndex)
    }

    protected void updateValue(Object newValue) {
        value = newValue
    }

    public MultiDimensionalParameterHolder clone() {
        MultiDimensionalParameterHolder holder = (MultiDimensionalParameterHolder) super.clone();
        holder.value = (AbstractMultiDimensionalParameter) value.clone()
        
        /*List<Integer> columnIndicesOfTypeDateTime = getColumnIndexOfTypeDateTime(holder)
        if (!columnIndicesOfTypeDateTime.isEmpty()) {
            // do a "deep clone" if any of the columns contains values of type DateTime
            IMultiDimensionalConstraints constraints = (IMultiDimensionalConstraints) value.constraints.class.newInstance()
            List titles = []
            for (int column = 0; column < value.getColumnCount(); column++) {
                titles << new String((String) value.titles[column])
            }
            List cellValues = []
            for (int column = 0; column < value.getColumnCount(); column++) {
                cellValues << []
                if (columnIndicesOfTypeDateTime.contains(column)) {
                    for (int row = value.getTitleRowCount(); row < value.getRowCount(); row++) {
                        cellValues[column][row - 1] = new DateTime(value.getValueAt(row, column).getMillis())
                    }
                }
                else {
                    for (int row = value.getTitleRowCount(); row < value.getRowCount(); row++) {
                        cellValues[column][row - 1] = value.getValueAt(row, column).clone()
                    }
                }
            }
            holder.value = new ConstrainedMultiDimensionalParameter(cellValues, titles, constraints)
        }
        else {
            // method does not work for DateTime values
            holder.value = (AbstractMultiDimensionalParameter) new GroovyShell(getClass().getClassLoader()).evaluate(value.toString())
        }*/
        return holder
    }

    private static List<Integer> getColumnIndexOfTypeDateTime(MultiDimensionalParameterHolder parameterHolder) {
        List<Integer> columnIndicesOfTypeDateTime = []
        if (parameterHolder.value instanceof ConstrainedMultiDimensionalParameter) {
            for (int column = 0; column < parameterHolder.value.getColumnCount(); column++) {
                if (parameterHolder.value.constraints.getColumnType(column).equals(DateTime.class)) {
                    columnIndicesOfTypeDateTime << column
                }
            }
        }
        return columnIndicesOfTypeDateTime
    }

    List<String> referencePaths(Class markerInterface, String refValue) {
        List<String> paths = new ArrayList()
        if ((value instanceof ConstrainedMultiDimensionalParameter)
                && ((ConstrainedMultiDimensionalParameter) value).referencePaths(markerInterface, refValue)) {
            paths.add(path)
        }
        else if ((value instanceof ComboBoxTableMultiDimensionalParameter) && markerInterface.is(value.markerClass)) {
            if (value.values.indexOf(refValue) > -1) {
                paths.add(path)
            }
        }
        return paths
    }

    List<String> updateReferenceValues(Class markerInterface, String oldValue, String newValue) {
        List<String> referencePaths = referencePaths(markerInterface, oldValue)
        if (referencePaths) {
            if (value instanceof ConstrainedMultiDimensionalParameter) {
                ((ConstrainedMultiDimensionalParameter) value).updateReferenceValues(markerInterface, oldValue, newValue)
            }
            else if (value instanceof ComboBoxTableMultiDimensionalParameter) {
                int row = value.values.indexOf(oldValue)
                row += value.getTitleRowCount()
                if (row > -1) {
                    value.setValueAt newValue, row, 0
                }
            }
        }
        return referencePaths
    }
}
