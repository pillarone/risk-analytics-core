package org.pillarone.riskanalytics.core.parameter

import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.parameter.comment.Tag

class ParameterizationTag {

    ParameterizationDAO parameterizationDAO
    Tag tag

    static belongsTo = ParameterizationDAO

    String toString() {
        tag
    }
}
