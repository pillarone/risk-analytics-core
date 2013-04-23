package org.pillarone.riskanalytics.core.model.migration

import groovy.transform.CompileStatic
import org.pillarone.riskanalytics.core.model.IModelVisitor
import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.parameterization.IParameterObject
import org.pillarone.riskanalytics.core.parameterization.ConstrainedMultiDimensionalParameter
import org.pillarone.riskanalytics.core.parameterization.IMultiDimensionalConstraints
import org.pillarone.riskanalytics.core.util.GroovyUtils
import java.util.Map.Entry
import org.pillarone.riskanalytics.core.model.ModelPath
import org.pillarone.riskanalytics.core.components.IResource

@CompileStatic
class ConstrainedMultiDimensionalParameterCollector implements IModelVisitor {

    List<ConstrainedMultiDimensionalParameter> result = []
    IMultiDimensionalConstraints constraints

    ConstrainedMultiDimensionalParameterCollector(IMultiDimensionalConstraints constraints) {
        this.constraints = constraints
    }

    void visitModel(Model model) {
    }

    void visitComponent(Component component, ModelPath path) {
        for (Entry<String, Object> entry in GroovyUtils.getProperties(component).entrySet()) {
            if (entry.key.startsWith("parm")) {
                checkValue(entry.value)
            }
        }
    }

    void visitResource(IResource resource, ModelPath path) {

    }

    void visitParameterObject(IParameterObject parameterObject, ModelPath path) {
        for (Entry entry in parameterObject.parameters.entrySet()) {
            checkValue(entry.value)
        }
    }

    private checkValue(def value) {
        if (value instanceof ConstrainedMultiDimensionalParameter) {
            ConstrainedMultiDimensionalParameter mdp = (ConstrainedMultiDimensionalParameter) value
            if (mdp.constraints.class.name == constraints.class.name) {
                result << mdp
            }
        }
    }


}
