package org.pillarone.riskanalytics.core

/**
 * Used to indicate that retrieving results has failed for some reason
 */
class RiskAnalyticsResultAccessException extends RuntimeException {

    RiskAnalyticsResultAccessException(String message) {
        super(message)
    }

    RiskAnalyticsResultAccessException(String message, Throwable cause) {
        super(message, cause)
    }
}
