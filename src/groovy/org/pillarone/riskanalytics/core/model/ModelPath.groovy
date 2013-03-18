package org.pillarone.riskanalytics.core.model


final class ModelPath {

    List<ModelPathComponent> pathComponents = []

    ModelPath append(ModelPathComponent pathComponent) {
        List copy = new ArrayList(pathComponents)
        copy << pathComponent
        return new ModelPath(pathComponents: copy)
    }

    @Override
    String toString() {
        return pathComponents.collect { it.toString() }.join(" - ")
    }


}
