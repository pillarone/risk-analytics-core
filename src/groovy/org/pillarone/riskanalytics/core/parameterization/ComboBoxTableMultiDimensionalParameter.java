package org.pillarone.riskanalytics.core.parameterization;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pillarone.riskanalytics.core.components.Component;
import org.pillarone.riskanalytics.core.model.Model;

import java.util.*;

/**
 * @deprecated Use {@link ConstrainedMultiDimensionalParameter} instead.
 */
public class ComboBoxTableMultiDimensionalParameter extends TableMultiDimensionalParameter implements IComboBoxBasedMultiDimensionalParameter {

    static Log LOG = LogFactory.getLog(ComboBoxTableMultiDimensionalParameter.class);

    private Map<String, Component> comboBoxValues = new HashMap<String, Component>();
    private Class markerClass;

    public ComboBoxTableMultiDimensionalParameter(List cellValues, List titles, Class markerClass) {
        super(cellValues, titles);
        this.markerClass = markerClass;
    }


    public void setSimulationModel(Model simulationModel) {
        super.setSimulationModel(simulationModel);
        if (simulationModel != null) {
            List<Component> markedComponents = simulationModel.getMarkedComponents(markerClass);
            comboBoxValues.clear();
            for (Component c : markedComponents) {
                comboBoxValues.put(c.getName(), c);
            }
            LOG.debug("Marker: " + markerClass + ", comboBoxValues: " + comboBoxValues.values());
        } else {
            comboBoxValues.clear();
        }
    }

    public void validateValues() {
        for (List list : values) {
            int i = 0;
            for (Object value : list) {
                if (!comboBoxValues.keySet().contains(value)) {
                    Object[] validValues = comboBoxValues.keySet().toArray();
                    if (validValues.length > 0) {
                        list.set(i, validValues[0]);
                    }
                }
                i++;
            }
        }
    }

    public Object getPossibleValues(int row, int column) {
        if (row == 0) {
            return getValueAt(row, column);
        } else {
            List<String> names = new ArrayList<String>();
            List<Component> markedComponents = simulationModel.getMarkedComponents(markerClass);
            for (Component c : markedComponents) {
                names.add(c.getName());
            }
            return names;
        }
    }


    public List getValuesAsObjects() {
        List result = new ArrayList();
        for (Object o : getValues()) {
            List l = new ArrayList();
            for (Object obj : (List) o) {
                if (!(o instanceof String && ((String) o).length() == 0)) {
                    l.add(comboBoxValues.get(obj));
                }

            }
            result.add(l);
        }
        return result;
    }

    /**
     * @param subList
     * @param removeNullElements
     * @return only the first list returned by getValuesAsObjects() and clears this list if the first element is null
     */
    public List getValuesAsObjects(int subList, boolean removeNullElements) {
        List result = (List) getValuesAsObjects().get(subList);
        if (removeNullElements && result.size() > 0) {
            for (int item = result.size() - 1; item >= 0; item--) {
                if (result.get(item) == null) {
                    result.remove(item);
                }
            }
        }
        return result;
    }

    public Class getMarkerClass() {
        return markerClass;
    }

    protected void appendAdditionalConstructorArguments(StringBuffer buffer) {
        super.appendAdditionalConstructorArguments(buffer);
        buffer.append(", ");
        buffer.append(markerClass.getName());
    }

    @Override
    public boolean supportsZeroRows() {
        return true;
    }

    @Override
    protected Object createDefaultValue(int row, int column, Object object) {
        List list = (List) getPossibleValues(1, 1);
        return list.size() > 0 ? list.get(0) : "";
    }

    @Override
    public ComboBoxTableMultiDimensionalParameter clone() throws CloneNotSupportedException {
        final ComboBoxTableMultiDimensionalParameter clone = (ComboBoxTableMultiDimensionalParameter) super.clone();
        clone.comboBoxValues = new HashMap<String, Component>();
        clone.markerClass = markerClass;
        clone.comboBoxValues.clear();
        return clone;
    }
}