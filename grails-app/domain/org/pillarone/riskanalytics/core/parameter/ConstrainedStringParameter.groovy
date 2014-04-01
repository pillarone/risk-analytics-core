package org.pillarone.riskanalytics.core.parameter

class ConstrainedStringParameter extends Parameter {

    String markerClass
    String parameterValue

    Class persistedClass() {
        ConstrainedStringParameter
    }
}