package org.pillarone.riskanalytics.core.parameter

import org.joda.time.DateTime
import org.joda.time.contrib.hibernate.PersistentDateTime

class DateParameter extends Parameter {

    DateTime dateValue

    Class persistedClass() {
        DateParameter
    }


    static mapping = {
        dateValue type: PersistentDateTime
    }
}
