package org.pillarone.riskanalytics.core.parameter


class ResourceParameter extends Parameter {

    String name
    String itemVersion
    String resourceClassName

    @Override
    Class persistedClass() {
        return ResourceParameter
    }


}
