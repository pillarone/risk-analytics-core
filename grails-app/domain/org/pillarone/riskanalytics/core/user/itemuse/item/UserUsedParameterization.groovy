package org.pillarone.riskanalytics.core.user.itemuse.item

import org.pillarone.riskanalytics.core.user.itemuse.UserUsedItem
import org.pillarone.riskanalytics.core.ParameterizationDAO

class UserUsedParameterization extends UserUsedItem {

    ParameterizationDAO parameterization

    static constraints = {
    }

    Class persistedClass() {
      UserUsedParameterization
    }
}
