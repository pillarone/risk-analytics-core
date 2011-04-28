package org.pillarone.riskanalytics.core.model

import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.parameterization.IParameterObject


public interface IModelVisitor {

    void visitModel(Model model)
    void visitComponent(Component component, ModelPath path)
    void visitParameterObject(IParameterObject parameterObject, ModelPath path)

}
