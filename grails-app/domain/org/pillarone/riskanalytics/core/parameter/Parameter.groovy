package org.pillarone.riskanalytics.core.parameter

import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.SimulationProfileDAO
import org.pillarone.riskanalytics.core.output.SimulationRun
import org.pillarone.riskanalytics.core.ResourceDAO

class Parameter {

    String path
    Integer periodIndex = 0

    static belongsTo = [
            ParameterizationDAO, ParameterEntry, ParameterObjectParameter,
            MultiDimensionalParameterValue, SimulationRun, ResourceDAO, SimulationProfileDAO
    ]

    Class persistedClass() {
        Parameter
    }


}
