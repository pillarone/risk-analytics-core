package org.pillarone.riskanalytics.core.test

import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.persistence.DateTimeMillisUserType
import org.pillarone.riskanalytics.core.util.DatabaseUtils


class DateTimeMillisUserTypeTest {

    DateTime theDate

    static constraints = {
        theDate(nullable: true)
    }

    static mapping = {
        theDate(type: DateTimeMillisUserType)
        if (DatabaseUtils.isOracleDatabase()) {
            table("date_datatype_test")
        }
    }
}
