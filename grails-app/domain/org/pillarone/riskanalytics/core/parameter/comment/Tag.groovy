package org.pillarone.riskanalytics.core.parameter.comment

import org.pillarone.riskanalytics.core.simulation.item.parameter.comment.EnumTagType

class Tag {

    public static final String LOCKED_TAG = "LOCKED"

    String name

    EnumTagType tagType = EnumTagType.COMMENT

    String toString() {
        name
    }

    boolean equals(Object obj) {
        if (obj instanceof Tag) {
            return name.equals(obj.name)
        }
        return false
    }

    static constraints = {
        name(unique: true)
    }

    int hashCode() {
        return name.hashCode()
    }


}
