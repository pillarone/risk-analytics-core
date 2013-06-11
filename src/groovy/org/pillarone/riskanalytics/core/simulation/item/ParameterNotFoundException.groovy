package org.pillarone.riskanalytics.core.simulation.item

import groovy.transform.CompileStatic

@CompileStatic
class ParameterNotFoundException extends IllegalArgumentException {

    ParameterNotFoundException(String s) {
        super(s)
    }
}
