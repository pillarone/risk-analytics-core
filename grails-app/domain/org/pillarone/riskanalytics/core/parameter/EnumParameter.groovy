package org.pillarone.riskanalytics.core.parameter

class EnumParameter extends Parameter {

    String parameterValue
    String parameterType

    Object parameterObject

    static transients = ['parameterObject']

    // todo: implements validation of type and value

    Class persistedClass() {
        EnumParameter
    }

}
