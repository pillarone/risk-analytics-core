package org.pillarone.riskanalytics.core.parameter

import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.output.SimulationRun

class Parameter {

    String path
    Integer periodIndex = 0

    static belongsTo = [
            ParameterizationDAO, ParameterEntry, ParameterObjectParameter,
            MultiDimensionalParameterValue, SimulationRun
    ]

    Class persistedClass() {
        Parameter
    }


}
