package org.pillarone.riskanalytics.core.parameterization;

import org.pillarone.riskanalytics.core.model.IModelVisitor;

import java.util.Map;
import java.util.Set;


public abstract class AbstractParameterObject implements IParameterObject {

    public void accept(IModelVisitor visitor) {
        visitor.visitParameterObject(this);
        Set<Map.Entry> entries = getParameters().entrySet();
        for (Map.Entry entry : entries) {
            if (entry.getValue() instanceof IParameterObject) {
                IParameterObject parameterObject = (IParameterObject) entry.getValue();
                parameterObject.accept(visitor);
            }
        }
    }
}
