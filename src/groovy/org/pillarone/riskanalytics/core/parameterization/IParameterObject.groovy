package org.pillarone.riskanalytics.core.parameterization

import org.pillarone.riskanalytics.core.model.IModelVisitor

interface IParameterObject {
    IParameterObjectClassifier getType()

    Map getParameters()

    void accept(IModelVisitor visitor)

}