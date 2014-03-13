package org.pillarone.riskanalytics.core

import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.output.ResultConfigurationDAO
import org.pillarone.riskanalytics.core.parameter.Parameter
import org.pillarone.riskanalytics.core.persistence.DateTimeMillisUserType
import org.pillarone.riskanalytics.core.user.Person

class SimulationProfileDAO {

    //mandatory
    String name
    String modelClassName
    boolean forPublic = false

    //optional
    ResultConfigurationDAO template
    Integer numberOfIterations
    Integer randomSeed
    static hasMany = [runtimeParameters: Parameter]

    //technically
    DateTime creationDate
    DateTime modificationDate
    Person lastUpdater
    Person creator

    static constraints = {
        name unique: ['modelClassName'], blank: false
        modelClassName blank: false
        template nullable: true
        numberOfIterations nullable: true
        randomSeed nullable: true
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

    @Override
    String toString() {
        "$name ($modelClassName)"
    }

    static namedQueries = {
        withModelClass { Class modelClass ->
            eq('modelClassName', modelClass?.name)
        }
        withCreator { Person creator ->
            eq('creator', creator)
        }
        withForPublic { boolean forPublic ->
            eq('forPublic', forPublic)
        }
        withCreatorOrForPublic { Person creator ->
            or {
                withForPublic(true)
                withCreator(creator)
            }
        }
    }
}