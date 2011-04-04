package org.pillarone.riskanalytics.core.model.migration

import org.pillarone.riskanalytics.core.model.IModelVisitor
import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.parameterization.IParameterObject
import org.pillarone.riskanalytics.core.parameterization.ConstrainedMultiDimensionalParameter
import org.pillarone.riskanalytics.core.parameterization.IMultiDimensionalConstraints
import org.pillarone.riskanalytics.core.util.GroovyUtils
import java.util.Map.Entry


class ConstrainedMultiDimensionalParameterCollector implements IModelVisitor {

    List<ConstrainedMultiDimensionalParameter> result = []
    IMultiDimensionalConstraints constraints

    ConstrainedMultiDimensionalParameterCollector(IMultiDimensionalConstraints constraints) {
        this.constraints = constraints
    }

    void visitModel(Model model) {
    }

    void visitComponent(Component component) {
        for (Entry<String, Object> entry in GroovyUtils.getProperties(component).entrySet()) {
            if (entry.key.startsWith("parm")) {
                checkValue(entry.value)
            }
        }
    }

    void visitParameterObject(IParameterObject parameterObject) {
        for (Entry<String, Object> entry in parameterObject.parameters) {
            checkValue(entry.value)
        }
    }

    private checkValue(def value) {
        if (value instanceof ConstrainedMultiDimensionalParameter) {
            if (value.constraints.class.name == constraints.class.name) {
                result << value
            }
        }
    }


}
