package org.pillarone.riskanalytics.core.simulation;

/**
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
public class InvalidParameterException extends SimulationException {

    public InvalidParameterException(String message) {
        super(message);
    }

    public InvalidParameterException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidParameterException(Throwable cause) {
        super(cause);
    }
}
