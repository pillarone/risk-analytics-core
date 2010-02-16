package org.pillarone.riskanalytics.core.parameter

import org.pillarone.riskanalytics.core.parameter.Parameter
import org.pillarone.riskanalytics.core.parameterization.ConstrainedString

class ConstrainedStringParameter extends Parameter {

    String markerClass
    String parameterValue

    Class persistedClass() {
        ConstrainedStringParameter
    }

}