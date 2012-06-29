package org.pillarone.riskanalytics.core.components;

import org.pillarone.riskanalytics.core.model.IModelVisitor;
import org.pillarone.riskanalytics.core.model.ModelPath;
import org.pillarone.riskanalytics.core.parameterization.IParameterObjectClassifier;

import java.util.List;

public interface IResource {

    void useDefault();

    void accept(IModelVisitor modelVisitor, ModelPath path);

    List<IParameterObjectClassifier> configureClassifier(String path, List<IParameterObjectClassifier> classifiers);

}
