package org.pillarone.riskanalytics.core

import org.pillarone.riskanalytics.core.output.ConfigObjectHolder

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
    }
}