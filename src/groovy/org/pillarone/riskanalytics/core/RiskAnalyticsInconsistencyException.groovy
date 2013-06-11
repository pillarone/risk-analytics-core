package org.pillarone.riskanalytics.core

import groovy.transform.CompileStatic

/**
 * Used to indicate that an internal consistency check has failed
 */
@CompileStatic
class RiskAnalyticsInconsistencyException extends RuntimeException {

    RiskAnalyticsInconsistencyException(String message) {
        super(message)
    }

    RiskAnalyticsInconsistencyException(String message, Throwable cause) {
        super(message, cause)
    }
}
