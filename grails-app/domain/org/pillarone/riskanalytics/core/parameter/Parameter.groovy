package org.pillarone.riskanalytics.core.parameter

import org.pillarone.riskanalytics.core.ParameterizationDAO

class Parameter {

    String path
    Integer periodIndex = 0

    static belongsTo = [ParameterizationDAO, ParameterEntry, ParameterObjectParameter, MultiDimensionalParameterValue]

    Class persistedClass() {
        Parameter
    }


}
