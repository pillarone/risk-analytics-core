package org.pillarone.riskanalytics.core

import org.pillarone.riskanalytics.core.output.ConfigObjectHolder
import org.pillarone.riskanalytics.core.util.DatabaseUtils

class ModelStructureDAO {

    String name
    String itemVersion
    String modelClassName
    String comment
    ConfigObjectHolder stringData

    static constraints = {
        name()
        comment(nullable: true, blank: true)
    }

    static mapping = {
        stringData lazy: true
        if(DatabaseUtils.isOracleDatabase()) {
            comment(column: 'comment_value')
        }
    }
}