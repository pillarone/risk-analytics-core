package org.pillarone.riskanalytics.core.parameter

import org.pillarone.riskanalytics.core.util.DatabaseUtils

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

    static mapping = {
        if (DatabaseUtils.isOracleDatabase()) {
            table('mdp_value')
            row(column: 'row_number')
            multiDimensionalParameter(column: 'mdp_id')
        }
    }

}