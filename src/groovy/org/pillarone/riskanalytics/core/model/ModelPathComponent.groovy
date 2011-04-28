package org.pillarone.riskanalytics.core.model


class ModelPathComponent {

    Class type
    String name

    ModelPathComponent(String name, Class type) {
        this.name = name
        this.type = type
    }

    @Override
    String toString() {
        "$name: [$type.simpleName]"
    }


}
