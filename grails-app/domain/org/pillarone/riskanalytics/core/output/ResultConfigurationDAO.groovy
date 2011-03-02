package org.pillarone.riskanalytics.core.output

import org.pillarone.riskanalytics.core.user.Person
import org.pillarone.riskanalytics.core.ModelDAO

public class ResultConfigurationDAO {

    String name
    String comment
    String modelClassName
    ModelDAO model
    String itemVersion

    Date creationDate
    Date modificationDate
    Person creator
    Person lastUpdater

    Set<CollectorInformation> collectorInformation

    static hasMany = [collectorInformation: CollectorInformation]

    static constraints = {
        comment(nullable: true)
        creationDate(nullable: true)
        model(nullable: true)
        modificationDate(nullable: true)
        creator nullable: true
        lastUpdater nullable: true
    }

    static mapping = {
        creator lazy: false
        lastUpdater lazy: false
    }
}