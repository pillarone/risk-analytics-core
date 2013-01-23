package org.pillarone.riskanalytics.core.wiring


class WiringException extends RuntimeException {

    WiringException() {
    }

    WiringException(Throwable cause) {
        super(cause)
    }

    WiringException(String message) {
        super(message)
    }

    WiringException(String message, Throwable cause) {
        super(message, cause)
    }
}
