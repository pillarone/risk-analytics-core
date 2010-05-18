package org.pillarone.riskanalytics.core

import org.pillarone.riskanalytics.core.parameter.Parameter
import org.pillarone.riskanalytics.core.user.Person

class ParameterizationDAO {

    String name
    String modelClassName
    String itemVersion
    Integer periodCount

    String comment
    String periodLabels
    Date creationDate
    Date modificationDate
    Person creator
    Person lastUpdater
    boolean valid

    static hasMany = [parameters: Parameter]


    static constraints = {
        name()
        comment(nullable: true, blank: true)
        periodLabels(nullable: true, blank: true)
        creationDate nullable: true
        modificationDate nullable: true
        creator nullable: true
        lastUpdater nullable: true
    }

    String toString() {
        "$name v$itemVersion"
    }

    /**
     * Returns a persisted ParameterizationDAO.
     * A parameterization can be uniquely identified by Name, Model & Version.
     */
    static ParameterizationDAO find(String name, String modelClassName, String versionNumber) {
        def criteria = ParameterizationDAO.createCriteria()
        //TODO: throw exception when there is more than one result? why can modelclass be null?
        def results = criteria.list {
            eq('name', name)
            eq('itemVersion', versionNumber)
            if (modelClassName != null)
                eq('modelClassName', modelClassName)
        }
        return results.size() > 0 ? results.get(0) : null
    }


}
