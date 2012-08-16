package org.pillarone.riskanalytics.core.example.component

import org.pillarone.riskanalytics.core.components.GlobalParameterComponent
import org.pillarone.riskanalytics.core.parameterization.global.Global

class ExampleParameterComponent extends GlobalParameterComponent {

    @Global(identifier = "int")
    int getInteger() {
        1
    }

    @Global(identifier = "string")
    String getString() {
        "string"
    }

    @Global(identifier = "sanityChecks")
    boolean getSanityChecks() {
        true
    }

    def nonGlobalMethod() {
        return new Object()
    }

}
