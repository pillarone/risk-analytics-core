package org.pillarone.riskanalytics.core.output

import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.ModelDAO
import org.pillarone.riskanalytics.core.persistence.DateTimeMillisUserType
import org.pillarone.riskanalytics.core.user.Person
import org.pillarone.riskanalytics.core.util.DatabaseUtils

public class ResultConfigurationDAO {

    String name
    String comment
    String modelClassName
    ModelDAO model
    String itemVersion

    DateTime creationDate
    DateTime modificationDate
    Person creator
    Person lastUpdater

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
        creationDate type: DateTimeMillisUserType
        modificationDate type: DateTimeMillisUserType
        if (DatabaseUtils.oracleDatabase) {
            comment(column: 'comment_value')
        }
    }

    /**
     * Returns a persisted ResultConfigurationDAO.
     * A parameterization can be uniquely identified by Name, Model & Version.
     */
    static ResultConfigurationDAO find(String name, String modelClassName, String versionNumber) {
        def criteria = createCriteria()
        return criteria.get {
            eq('name', name)
            eq('itemVersion', versionNumber)
            eq('modelClassName', modelClassName)
        }
    }
}