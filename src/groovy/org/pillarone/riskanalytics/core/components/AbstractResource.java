package org.pillarone.riskanalytics.core.components;

import org.pillarone.riskanalytics.core.model.IModelVisitor;
import org.pillarone.riskanalytics.core.model.ModelPath;
import org.pillarone.riskanalytics.core.model.ModelPathComponent;
import org.pillarone.riskanalytics.core.parameterization.IParameterObject;
import org.pillarone.riskanalytics.core.parameterization.IParameterObjectClassifier;
import org.pillarone.riskanalytics.core.util.GroovyUtils;
import java.util.List;
import java.util.Map;

public abstract class AbstractResource implements IResource {

    public void accept(IModelVisitor modelVisitor, ModelPath path) {
        modelVisitor.visitResource(this, path);
        for (Map.Entry<String, Object> property : GroovyUtils.getProperties(this).entrySet()) {
            Object propertyValue = property.getValue();
            if (property.getKey().startsWith("parm")) {
                if (propertyValue instanceof IParameterObject) {
                    IParameterObject parameterObject = (IParameterObject) propertyValue;
                    parameterObject.accept(modelVisitor, path.append(new ModelPathComponent(property.getKey(), parameterObject.getType().getClass())));
                } else if(propertyValue instanceof ResourceHolder) {
                    IResource resource = ((ResourceHolder) propertyValue).getResource();
                    resource.accept(modelVisitor, path.append(new ModelPathComponent(property.getKey(), resource.getClass())));
                }
            }
        }
    }

    public List<IParameterObjectClassifier> configureClassifier(String path, List<IParameterObjectClassifier> classifiers) {
        return classifiers;
    }
}
