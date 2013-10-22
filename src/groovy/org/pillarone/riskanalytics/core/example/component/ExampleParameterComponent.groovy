package org.pillarone.riskanalytics.core.example.component

import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.components.GlobalParameterComponent
import org.pillarone.riskanalytics.core.parameterization.global.Global

class ExampleParameterComponent extends GlobalParameterComponent {

    private DateTime parmProjectionStartDate = new DateTime(new DateTime().getYear()+1,1,1,0,0,0,0);
    private boolean parmRunOffAfterFirstPeriod = false;

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
