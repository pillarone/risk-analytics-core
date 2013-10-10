package org.pillarone.riskanalytics.core.parameterization

import org.pillarone.riskanalytics.core.model.IModelVisitor
import org.pillarone.riskanalytics.core.model.ModelPath

interface IParameterObject extends Serializable{
    IParameterObjectClassifier getType()

    Map getParameters()

    void accept(IModelVisitor visitor, ModelPath path)

}