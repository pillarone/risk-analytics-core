package org.pillarone.riskanalytics.core.parameterization;

import org.joda.time.DateTime;
import org.pillarone.riskanalytics.core.components.*;
import org.pillarone.riskanalytics.core.model.Model;
import org.pillarone.riskanalytics.core.simulation.item.VersionNumber;
import org.pillarone.riskanalytics.core.simulation.item.parameter.ResourceParameterHolder;
import org.pillarone.riskanalytics.core.simulation.item.parameter.ResourceParameterHolder.NameVersionPair;
import org.pillarone.riskanalytics.core.util.GroovyUtils;

import java.math.BigDecimal;
import java.util.*;

public class ConstrainedMultiDimensionalParameter extends TableMultiDimensionalParameter implements IMarkerBasedMultiDimensionalParameter {

    private IMultiDimensionalConstraints constraints;
    private Map<Integer, Map<String, Component>> comboBoxValues = new HashMap<Integer, Map<String, Component>>();

    public ConstrainedMultiDimensionalParameter(List cellValues, List titles, IMultiDimensionalConstraints constraints) {
        super(cellValues, titles);
        this.constraints = constraints;
    }


    @Override
    public void setValueAt(Object value, int row, int column) {
        if (constraints.matches(row, column, value)) {
            super.setValueAt(value, row, column);
        } else {
            throw new IllegalArgumentException("Value does not pass constraints");
        }
    }

    public IMultiDimensionalConstraints getConstraints() {
        return constraints;
    }

    public void setSimulationModel(Model simulationModel) {
        this.simulationModel = simulationModel;
        if (simulationModel != null) {
            for (int i = 0; i < getValueColumnCount(); i++) {
                final Class columnType = constraints.getColumnType(i);
                if (IComponentMarker.class.isAssignableFrom(columnType)) {
                    Map<String, Component> result = new HashMap<String, Component>();
                    List<Component> componentsOfType = simulationModel.getMarkedComponents(columnType);
                    for (Component component : componentsOfType) {
                        result.put(component.getName(), component);
                    }
                    comboBoxValues.put(i, result);
                }
            }
        } else {
            comboBoxValues.clear();
        }
    }

    public void validateValues() {
        // column 0 for the index 0,1,2,3,...
        int col = 0;
        for (List list : values) {
            int row = 0;
            for (Object value : list) {
                Object possibleValues = getPossibleValues(row + 1, col);
                if (possibleValues instanceof List) {
                    validate(value, (List<String>) possibleValues, list, row);
                }
                row++;
            }
            col++;
        }
    }

    public boolean isMarkerCell(int row, int column) {
        if (row > 0) {
            Class columnType = constraints.getColumnType(column);
            return IComponentMarker.class.isAssignableFrom(columnType);
        }

        return false;
    }

    protected void validate(Object value, List<String> validValues, List list, int currentRow) {
        if (value instanceof String) {
            if (!validValues.contains(value)) {
                if (validValues.size() > 0) {
                    list.set(currentRow, validValues.get(0));
                }
            }
        } else if (value instanceof ResourceHolder) {
            ResourceHolder holder = (ResourceHolder) value;
            NameVersionPair pair = new NameVersionPair(holder.getName(), holder.getVersion().toString());
            if (!validValues.contains(pair.toString())) {
                if (validValues.size() > 0) {
                    pair = NameVersionPair.parse(validValues.get(0));
                    holder = new ResourceHolder(holder.getResourceClass(), pair.getName(), new VersionNumber(pair.getVersion()));
                    list.set(currentRow, holder);
                }
            }
        }
    }

    /**
     * @param column
     * @return selected component or resource instances if column contains either of them or values.get(column) in all other cases
     */
    public List getValuesAsObjects(int column) {
        final Class columnType = constraints.getColumnType(column);
        List result = new LinkedList();
        if (IComponentMarker.class.isAssignableFrom(columnType)) {
            Map<String, Component> componentsOfType = comboBoxValues.get(column);
            List<String> selectedValues = values.get(column);
            for (String selectedValue : selectedValues) {
                if (!selectedValue.trim().isEmpty()) {
                    result.add(componentsOfType.get(selectedValue));
                }
            }
        } else if (IResource.class.isAssignableFrom(columnType)) {
            List<ResourceHolder> selectedValues = values.get(column);
            for (ResourceHolder selectedValue : selectedValues) {
                result.add(ResourceRegistry.getResourceInstance(selectedValue));
            }
        } else {
            result.addAll(values.get(column));
        }

        return result;
    }

    /**
     * @param row    number including title and value rows
     * @param column
     * @return selected component or resource instance if the column contains either of them or the same as getValueAt in all other cases
     */
    public Object getValueAtAsObject(int row, int column) {
        int dataRow = row - getTitleRowCount();
        final Class columnType = constraints.getColumnType(column);
        if (IComponentMarker.class.isAssignableFrom(columnType)) {
            String selectedValue = (String) values.get(column).get(dataRow);
            Map<String, Component> componentsOfType = comboBoxValues.get(column);
            return componentsOfType.get(selectedValue);
        } else if (IResource.class.isAssignableFrom(columnType)) {
            ResourceHolder selectedValue = (ResourceHolder) values.get(column).get(dataRow);
            return ResourceRegistry.getResourceInstance(selectedValue);
        } else {
            return getValueAt(row, column);
        }
    }

    @Override
    public Object getPossibleValues(int row, int column) {
        if (row == 0) {
            return new Object();
        }
        Class columnClass = constraints.getColumnType(column);
        if (IComponentMarker.class.isAssignableFrom(columnClass)) {
            List<String> names = new ArrayList<String>();
            List<Component> components = simulationModel.getMarkedComponents(columnClass);
            for (Component component : components) {
                names.add(component.getName());
            }
            if (constraints.emptyComponentSelectionAllowed(column)) {
                names.add(0, "");
            }
            return names;
        } else if (columnClass.isEnum()) {
            return GroovyUtils.getEnumValuesFromClass(columnClass);
        } else if (IResource.class.isAssignableFrom(columnClass)) {
            return GroovyUtils.getValuesForResourceClass(columnClass);
        } else {
            return new Object();
        }
    }

    protected void appendAdditionalConstructorArguments(StringBuffer buffer) {
        super.appendAdditionalConstructorArguments(buffer);
        buffer.append(", ");
        buffer.append("org.pillarone.riskanalytics.core.parameterization.ConstraintsFactory.getConstraints('").append(constraints.getName()).append("')");
    }

    @Override
    public boolean supportsZeroRows() {
        return true;
    }

    @Override
    protected Object createDefaultValue(int row, int column, Object object) {
        Object result = "";
        Class columnClass = constraints.getColumnType(column);
        if (IComponentMarker.class.isAssignableFrom(columnClass)) {
            List list = (List) getPossibleValues(row + 1, column);
            if (list != null && list.size() > 0)
                result = list.get(0);
        } else if (columnClass == BigDecimal.class) {
            result = new BigDecimal(0);
        } else if (columnClass == Double.class) {
            result = 0d;
        } else if (columnClass == Integer.class) {
            result = 0;
        } else if (columnClass == DateTime.class) {
            result = new DateTime(new DateTime().getYear(), 1, 1, 0, 0, 0, 0);
        } else if (columnClass.isEnum()) {
            result = GroovyUtils.getEnumValuesFromClass(columnClass).get(0);
        } else if (IResource.class.isAssignableFrom(columnClass)) {
            final List<String> values = GroovyUtils.getValuesForResourceClass(columnClass);
            NameVersionPair pair = new NameVersionPair("", "1");
            if (!values.isEmpty()) {
                pair = ResourceParameterHolder.NameVersionPair.parse(values.get(0));
            }
            result = new ResourceHolder(columnClass, pair.getName(), new VersionNumber(pair.getVersion()));
        } else {
            try {
                result = columnClass.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(columnClass.getSimpleName() + " not supported as column type", e);
            }
        }
        return result;
    }

    @Override
    public ConstrainedMultiDimensionalParameter clone() throws CloneNotSupportedException {
        final ConstrainedMultiDimensionalParameter clone = (ConstrainedMultiDimensionalParameter) super.clone();
        clone.comboBoxValues = new HashMap<Integer, Map<String, Component>>();
        clone.constraints = constraints;
        return clone;
    }

    @Override
    public int getValueColumnCount() {
        return titles.size();
    }

    public boolean referencePaths(Class markerInterface, String value) {
        Integer column = constraints.getColumnIndex(markerInterface);
        return (column != null && values.get(column).indexOf(value) > -1);
    }

    public boolean updateReferenceValues(Class markerInterface, String oldValue, String newValue) {
        boolean atLeastOneUpdated = false;
        for (int column = getTitleColumnCount(); column < getColumnCount(); column++) {
            Class columnType = constraints.getColumnType(column);
            if (markerInterface.isAssignableFrom(columnType)) {
                for (int row = getTitleRowCount(); row < getRowCount(); row++) {
                    String value = (String) getValueAt(row, column);
                    if (value.equals(oldValue)) {
                        setValueAt(newValue, row, column);
                        atLeastOneUpdated = true;
                    }
                }
            }
        }
        return atLeastOneUpdated;
    }
}