package org.pillarone.riskanalytics.core.wiring;

public class WiringException extends RuntimeException {
    public WiringException() {
    }

    public WiringException(Throwable cause) {
        super(cause);
    }

    public WiringException(String message) {
        super(message);
    }

    public WiringException(String message, Throwable cause) {
        super(message, cause);
    }
}
