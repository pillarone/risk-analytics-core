package org.pillarone.riskanalytics.core.parameter

import org.joda.time.DateTime
import org.joda.time.contrib.hibernate.PersistentDateTime

class DateParameter extends Parameter {

    DateTime dateValue

    public Object getParameterInstance() {
        return dateValue
    }

    public void setParameterInstance(def value) {
        if (value instanceof DateTime) {
            dateValue = value
        } else {
            super.setParameterInstance(value)
        }
    }

    public void setParameterInstance(DateTime value) {
        dateValue = value
    }

    Class persistedClass() {
        DateParameter
    }


    static mapping = {
        dateValue type: PersistentDateTime
    }
}
