package org.pillarone.riskanalytics.core.model

import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.parameterization.IParameterObject
import org.pillarone.riskanalytics.core.components.IResource


public interface IModelVisitor {

    void visitModel(Model model)
    void visitComponent(Component component, ModelPath path)
    void visitResource(IResource resource, ModelPath path)
    void visitParameterObject(IParameterObject parameterObject, ModelPath path)

}
