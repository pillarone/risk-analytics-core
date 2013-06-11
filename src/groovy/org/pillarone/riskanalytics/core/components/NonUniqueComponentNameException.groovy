package org.pillarone.riskanalytics.core.components

import groovy.transform.CompileStatic

@CompileStatic
class NonUniqueComponentNameException extends Exception {

    NonUniqueComponentNameException(String s) {
        super(s)
    }

}
