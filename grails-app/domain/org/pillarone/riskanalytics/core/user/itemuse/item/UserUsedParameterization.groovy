package org.pillarone.riskanalytics.core.user.itemuse.item

import org.pillarone.riskanalytics.core.user.itemuse.UserUsedItem
import org.pillarone.riskanalytics.core.ParameterizationDAO
import grails.util.Environment

class UserUsedParameterization extends UserUsedItem {

    ParameterizationDAO parameterization

    static constraints = {
        if(Environment.current == Environment.TEST) {
            user(nullable: true)
        }
    }

    Class persistedClass() {
      UserUsedParameterization
    }
}
