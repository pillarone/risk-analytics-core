package org.pillarone.riskanalytics.core

import groovy.transform.CompileStatic

/**
 * Thrown when the reporting subsystem detects a terminal problem
 */
@CompileStatic
class RiskAnalyticsReportingException extends RuntimeException {

    RiskAnalyticsReportingException(String message) {
        super(message)
    }

    RiskAnalyticsReportingException(String message, Throwable cause) {
        super(message, cause)
    }
}
