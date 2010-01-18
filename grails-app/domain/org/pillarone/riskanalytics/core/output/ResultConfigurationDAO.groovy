package org.pillarone.riskanalytics.core.output

public class ResultConfigurationDAO {

    String name
    String comment
    String modelClassName
    String itemVersion

    Date creationDate
    Date modificationDate

    Set<CollectorInformation> collectorInformation


    static hasMany = [collectorInformation: CollectorInformation]

    static constraints = {
        comment(nullable: true)
        creationDate(nullable: true)
        modificationDate(nullable: true)
    }
}