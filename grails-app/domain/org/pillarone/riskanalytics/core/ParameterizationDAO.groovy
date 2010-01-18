package org.pillarone.riskanalytics.core

import org.pillarone.riskanalytics.core.parameter.Parameter

class ParameterizationDAO {

    String name
    String modelClassName
    String itemVersion
    Integer periodCount

    String comment
    String periodLabels
    Date creationDate
    Date modificationDate
    boolean valid

    static hasMany = [parameters: Parameter]


    static constraints = {
        name()
        comment(nullable: true, blank: true)
        periodLabels(nullable: true, blank: true)
        creationDate nullable: true
        modificationDate nullable: true
    }

    String toString() {
        "$name v$itemVersion"
    }


}
