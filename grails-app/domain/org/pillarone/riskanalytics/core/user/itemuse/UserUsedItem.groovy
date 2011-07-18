package org.pillarone.riskanalytics.core.user.itemuse

import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.persistence.DateTimeMillisUserType
import org.pillarone.riskanalytics.core.user.Person

class UserUsedItem {

    DateTime time
    Person user

    static constraints = {
    }

    static mapping = {
        time type: DateTimeMillisUserType
    }
}
