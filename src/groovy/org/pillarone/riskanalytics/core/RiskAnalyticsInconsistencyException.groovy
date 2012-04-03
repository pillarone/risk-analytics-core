package org.pillarone.riskanalytics.core

/**
 * Used to indicate that an internal consistency check has failed
 */
class RiskAnalyticsInconsistencyException extends RuntimeException {

    RiskAnalyticsInconsistencyException(String message) {
        super(message)
    }

    RiskAnalyticsInconsistencyException(String message, Throwable cause) {
        super(message, cause)
    }
}
