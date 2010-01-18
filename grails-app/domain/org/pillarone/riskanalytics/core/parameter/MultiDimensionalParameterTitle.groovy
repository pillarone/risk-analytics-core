package org.pillarone.riskanalytics.core.parameter

import org.pillarone.riskanalytics.core.parameter.MultiDimensionalParameter

class MultiDimensionalParameterTitle {

    Integer row
    Integer col
    String title

    static belongsTo = [multiDimensionalParameter: MultiDimensionalParameter]

    static constraints = {
        //todo validator
        row()
        col()
    }

}