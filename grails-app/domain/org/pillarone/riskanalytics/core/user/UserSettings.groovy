package org.pillarone.riskanalytics.core.user

class UserSettings {

    String language

    static belongsTo = Person

    static constraints = {
        language()
    }
}
