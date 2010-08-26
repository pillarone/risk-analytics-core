package org.pillarone.riskanalytics.core.parameter.comment


class Tag {

    String name

    String toString() {
        name
    }

    boolean equals(Object obj) {
        if (obj instanceof Tag) {
            return name.equals(obj.name)
        }
        return false
    }

    int hashCode() {
        return name.hashCode()
    }


}
