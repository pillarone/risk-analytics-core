package org.pillarone.riskanalytics.core.model.migration

import groovy.transform.CompileStatic
import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.components.IResource
import org.pillarone.riskanalytics.core.model.IModelVisitor
import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.model.ModelPath
import org.pillarone.riskanalytics.core.parameterization.AbstractMultiDimensionalParameter
import org.pillarone.riskanalytics.core.parameterization.ConstrainedMultiDimensionalParameter
import org.pillarone.riskanalytics.core.parameterization.IParameterObject
import org.pillarone.riskanalytics.core.util.GroovyUtils
import org.pillarone.riskanalytics.core.components.IComponentMarker
import org.pillarone.riskanalytics.core.parameterization.ComboBoxTableMultiDimensionalParameter
import org.pillarone.riskanalytics.core.parameterization.ComboBoxMatrixMultiDimensionalParameter

@CompileStatic
class MarkerBasedMultiDimensionalParameterCollector implements IModelVisitor {

    List<AbstractMultiDimensionalParameter> result = []

    void visitModel(Model model) {
    }

    void visitComponent(Component component, ModelPath path) {
        for (Map.Entry<String, Object> entry in GroovyUtils.getProperties(component).entrySet()) {
            if (entry.key.startsWith("parm")) {
                checkValue(entry.value)
            }
        }
    }

    void visitResource(IResource resource, ModelPath path) {

    }

    void visitParameterObject(IParameterObject parameterObject, ModelPath path) {
        for (Map.Entry entry in parameterObject.parameters.entrySet()) {
            checkValue(entry.value)
        }
    }

    private checkValue(def value) {
        if (value instanceof ConstrainedMultiDimensionalParameter) {
            ConstrainedMultiDimensionalParameter mdp = (ConstrainedMultiDimensionalParameter) value
            for (int i = 0; i < mdp.columnCount; i++) {
                if (IComponentMarker.isAssignableFrom(mdp.constraints.getColumnType(i))) {
                    result << mdp
                    return
                }
            }
        } else if (value instanceof ComboBoxTableMultiDimensionalParameter || value instanceof ComboBoxMatrixMultiDimensionalParameter) {
            result << (AbstractMultiDimensionalParameter) value
        }
    }
}
