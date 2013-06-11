package org.pillarone.riskanalytics.core.components

import groovy.transform.CompileStatic
import org.pillarone.riskanalytics.core.model.StochasticModel
import org.pillarone.riskanalytics.core.parameterization.IParameterObjectClassifier

@CompileStatic
class ResourceModelAdapter extends StochasticModel {

    IResource resource

    ResourceModelAdapter(IResource resource) {
        this.resource = resource
    }

    @Override
    void initComponents() { }

    @Override
    void wireComponents() { }

    @Override
    List<IParameterObjectClassifier> configureClassifier(String path, List<IParameterObjectClassifier> classifiers) {
        return resource.configureClassifier(path, classifiers)
    }
}
