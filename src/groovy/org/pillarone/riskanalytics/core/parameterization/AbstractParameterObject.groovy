package org.pillarone.riskanalytics.core.parameterization

import org.pillarone.riskanalytics.core.model.IModelVisitor


abstract class AbstractParameterObject implements IParameterObject {

    void accept(IModelVisitor visitor) {
        visitor.visitParameterObject(this)
        for (Map.Entry entry in getParameters().entrySet()) {
            if (entry.value instanceof IParameterObject) {
                entry.value.accept(visitor)
            }
        }
    }
}
