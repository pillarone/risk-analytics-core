package org.pillarone.riskanalytics.core.parameterization;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pillarone.riskanalytics.core.components.Component;
import org.pillarone.riskanalytics.core.model.Model;

import java.util.*;

public class ComboBoxTableMultiDimensionalParameter extends TableMultiDimensionalParameter implements IComboBoxBasedMultiDimensionalParameter {

    static Log LOG = LogFactory.getLog(ComboBoxTableMultiDimensionalParameter.class);

    private Map comboBoxValues = new HashMap();
    private Class markerClass;

    public ComboBoxTableMultiDimensionalParameter(List cellValues, List titles, Class markerClass) {
        super(cellValues, titles);
        this.markerClass = markerClass;
    }

    public ComboBoxTableMultiDimensionalParameter(String cellValues, List titles, Class markerClass) {
        super(cellValues, titles);
        this.markerClass = markerClass;
    }

    public void setSimulationModel(Model simulationModel) {
        super.setSimulationModel(simulationModel);
        for (Component c : simulationModel.getMarkedComponents(markerClass)) {
            comboBoxValues.put(normalizeName(c.getName()), c);
        }
        LOG.debug("Marker: " + markerClass + ", comboBoxValues: " + comboBoxValues.values());
        //TODO: (msp) enable when new simulation engine is used
//        checkAndCorrectValues();
    }

    protected void checkAndCorrectValues() {
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
        }
        else {
            List names = new LinkedList();
            for (Object c : comboBoxValues.values()) {
                names.add(normalizeName(((Component) c).getName()));
            }
            return names;
        }
    }


    public List getValuesAsObjects() {
        List result = new ArrayList();
        if (valuesConverted) {
            for (Object o : getValues()) {
                if (!(o instanceof String && ((String) o).length() == 0)) {
                    result.add(comboBoxValues.get(o));
                }
            }
        }
        else {
            for (Object o : getValues()) {
                List l = new ArrayList();
                for (Object obj : (List) o) {
                    if (!(o instanceof String && ((String) o).length() == 0)) {
                        l.add(comboBoxValues.get(obj));
                    }

                }
                result.add(l);
            }
        }
        return result;
    }

    // todo: this function is added due to PMO-492. It should become obsolete once the issue will be resolved properly
    public List getValuesAsObjects(Model simulationModel) {
        if (comboBoxValues.size() == 0) {
            setSimulationModel(simulationModel);
        }
        return getValuesAsObjects();
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
    protected Object createDefaultValue(int column) {
        List list = (List) getPossibleValues(1, 0);
        return list.get(0);
    }
}