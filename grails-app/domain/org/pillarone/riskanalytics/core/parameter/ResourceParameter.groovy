package org.pillarone.riskanalytics.core.parameter


class ResourceParameter extends Parameter {

    String name
    String itemVersion
    String resourceClassName

    @Override
    Class persistedClass() {
        return ResourceParameter
    }

    static constraints = {
        name(nullable: true)
        itemVersion(nullable: true)
    }


}
