package org.pillarone.riskanalytics.core.parameterization;

import org.pillarone.riskanalytics.core.model.IModelVisitor;
import org.pillarone.riskanalytics.core.model.ModelPath;
import org.pillarone.riskanalytics.core.model.ModelPathComponent;

import java.util.Map;
import java.util.Set;


public abstract class AbstractParameterObject implements IParameterObject {

    public void accept(IModelVisitor visitor, ModelPath path) {
        visitor.visitParameterObject(this, path);
        Set<Map.Entry> entries = getParameters().entrySet();
        for (Map.Entry entry : entries) {
            if (entry.getValue() instanceof IParameterObject) {
                IParameterObject parameterObject = (IParameterObject) entry.getValue();
                parameterObject.accept(visitor, path.append(new ModelPathComponent(entry.getKey().toString(), parameterObject.getType().getClass())));
            }
        }
    }
}
