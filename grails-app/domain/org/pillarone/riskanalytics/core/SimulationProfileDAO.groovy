package org.pillarone.riskanalytics.core

import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.output.ResultConfigurationDAO
import org.pillarone.riskanalytics.core.parameter.Parameter
import org.pillarone.riskanalytics.core.persistence.DateTimeMillisUserType
import org.pillarone.riskanalytics.core.user.Person

class SimulationProfileDAO {

    String name
    ResultConfigurationDAO template
    Integer numberOfIterations
    Integer randomSeed

    DateTime creationDate
    DateTime modificationDate
    Person lastUpdater
    Person creator

    static hasMany = [runtimeParameters: Parameter]

    static constraints = {
        name unique: true, blank: false
        template nullable: false
        randomSeed nullable: true
        numberOfIterations nullable: true
        modificationDate nullable: true
        lastUpdater nullable: true
        creator nullable: true
    }

    static mapping = {
        creationDate type: DateTimeMillisUserType
        modificationDate type: DateTimeMillisUserType
        creator lazy: false
        lastUpdater lazy: false
    }

    static namedQueries = {
        allNamesForModelClass { Class modelClass ->
            template {
                eq('modelClassName', modelClass.name)
            }
            projections {
                property 'name'
            }
            order('name')
        }
    }
}