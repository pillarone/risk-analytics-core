package org.pillarone.riskanalytics.core.parameter.comment

import org.pillarone.riskanalytics.core.ResourceDAO

class ResourceTag {

    ResourceDAO resourceDAO
    Tag tag

    static belongsTo = ResourceDAO

    String toString() {
        tag
    }
}
