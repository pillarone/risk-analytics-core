package org.pillarone.riskanalytics.core.parameter

import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.persistence.DateTimeMillisUserType

class DateParameter extends Parameter {

    DateTime dateValue

    Class persistedClass() {
        DateParameter
    }


    static mapping = {
        dateValue type: DateTimeMillisUserType
    }
}
