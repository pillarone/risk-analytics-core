package org.pillarone.riskanalytics.core.test

import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.persistence.DateTimeMillisUserType


class DateTimeMillisUserTypeTest {

    DateTime theDate

    static constraints = {
        theDate(nullable: true)
    }

    static mapping = {
        theDate(type: DateTimeMillisUserType)
    }
}
