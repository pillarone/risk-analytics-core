package org.pillarone.riskanalytics.core.example.component

import org.pillarone.riskanalytics.core.components.IResource

class ExampleResource implements IResource {

    boolean defaultCalled = false

    int parmInteger = 10
    String parmString = "test"

    void useDefault() {
        defaultCalled = true
    }


}
