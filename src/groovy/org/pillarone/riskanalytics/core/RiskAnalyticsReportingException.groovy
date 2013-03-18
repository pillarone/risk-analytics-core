package org.pillarone.riskanalytics.core

/**
 * Thrown when the reporting subsystem detects a terminal problem
 */
class RiskAnalyticsReportingException extends RuntimeException {

    RiskAnalyticsReportingException(String message) {
        super(message)
    }

    RiskAnalyticsReportingException(String message, Throwable cause) {
        super(message, cause)
    }
}
