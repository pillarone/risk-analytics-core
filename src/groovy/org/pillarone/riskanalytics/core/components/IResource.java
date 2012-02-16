package org.pillarone.riskanalytics.core.components;

import org.pillarone.riskanalytics.core.model.IModelVisitor;
import org.pillarone.riskanalytics.core.model.ModelPath;

public interface IResource {

    void useDefault();

    void accept(IModelVisitor modelVisitor, ModelPath path);
}
