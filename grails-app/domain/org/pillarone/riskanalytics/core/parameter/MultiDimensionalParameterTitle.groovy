package org.pillarone.riskanalytics.core.parameter

import org.pillarone.riskanalytics.core.util.DatabaseUtils

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

    static mapping = {
        if (DatabaseUtils.isOracleDatabase()) {
            table('mdp_title')
            row(column: 'row_number')
        }
    }

}