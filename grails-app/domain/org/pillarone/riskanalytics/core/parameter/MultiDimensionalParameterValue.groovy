package org.pillarone.riskanalytics.core.parameter

import org.pillarone.riskanalytics.core.parameter.MultiDimensionalParameter
import org.pillarone.riskanalytics.core.parameter.Parameter

class MultiDimensionalParameterValue {

    Integer row
    Integer col
    byte[] value

    static belongsTo = [multiDimensionalParameter: MultiDimensionalParameter]

    static constraints = {
        row(min: 0)
        col(min: 0)
        value(maxSize: 1000)
    }

}