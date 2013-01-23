package org.pillarone.riskanalytics.core.user.itemuse

import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.persistence.DateTimeMillisUserType
import org.pillarone.riskanalytics.core.user.Person
import grails.util.Environment

class UserUsedItem {

    DateTime time
    Person user

    static constraints = {
        if(Environment.current == Environment.TEST) {
            user(nullable: true)
        }
    }

    static mapping = {
        time type: DateTimeMillisUserType
    }
}
